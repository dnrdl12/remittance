package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.comm.config.AppAccountProperties;
import io.dnrdl12.remittance.comm.enums.AccountStatus;
import io.dnrdl12.remittance.comm.enums.ErrorCode;
import io.dnrdl12.remittance.comm.enums.TransferStatus;
import io.dnrdl12.remittance.comm.enums.EntryType;
import io.dnrdl12.remittance.comm.enums.FailCode;
import io.dnrdl12.remittance.comm.enums.ClientId;
import io.dnrdl12.remittance.comm.exception.RemittanceExceptionFactory;
import io.dnrdl12.remittance.dto.TransferDto;
import io.dnrdl12.remittance.entity.Account;
import io.dnrdl12.remittance.entity.FeePolicy;
import io.dnrdl12.remittance.entity.Ledger;
import io.dnrdl12.remittance.entity.Transfer;
import io.dnrdl12.remittance.repository.AccountRepository;
import io.dnrdl12.remittance.repository.LedgerRepository;
import io.dnrdl12.remittance.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : TransferServiceImpl
 * author         : JW.CHOI
 * date           : 2025-11-15
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-15        JW.CHOI           최초 생성
 * 2025-11-17        JW.CHOI           accountNumber 기반 조회 → accountSeq 사용으로 리팩토링
 */
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AppAccountProperties properties;
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final LedgerRepository ledgerRepository;
    private final BalanceSnapshotService balanceSnapshotService;

    /* ===================================================
     * 공통 유틸: 멱등성 체크
     * =================================================== */

    private Optional<Transfer> findExisting(ClientId clientId, String idempotencyKey) {
        if (clientId == null || idempotencyKey == null) {
            return Optional.empty();
        }
        return transferRepository.findByClientIdAndIdempotencyKey(clientId, idempotencyKey);
    }

    // 필요하다면, 멱등키 재사용에 대한 파라미터 검증까지 하고 싶으면 주석 해제 후 사용
    private void validateIdempotentRequest(Transfer existing,
                                           Long fromAccountSeq,
                                           Long toAccountSeq,
                                           Long amount) {
        if (existing == null) return;

        boolean same = true;
        if (fromAccountSeq != null && !fromAccountSeq.equals(existing.getFromAccountSeq())) {
            same = false;
        }
        if (toAccountSeq != null && !toAccountSeq.equals(existing.getToAccountSeq())) {
            same = false;
        }
        if (amount != null && !amount.equals(existing.getAmount())) {
            same = false;
        }

        if (!same) {
            // 멱등키를 다른 파라미터로 재사용한 경우 → 클라이언트 버그
            throw RemittanceExceptionFactory.of(
                    ErrorCode.CUSTOM_ERROR,
                    "이미 사용된 멱등키입니다. 요청 파라미터가 기존과 다릅니다."
            );
        }
    }

    /* ===================================================
     * 공통 유틸: 계좌 상태 검증
     * =================================================== */

    /**
     * 출금 계좌 검증용 (출금/이체 from 계좌)
     */
    private void validateDebitAccount(Account account, TransferDto.BaseReq req,
                                      Long toAccountSeq, Long amount) {

        AccountStatus status = account.getAccountStatus();
        if (status == AccountStatus.NORMAL)  return;

        // 실패 이력 먼저 남김
        Transfer failed = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(account.getAccountSeq())
                .toAccountSeq(toAccountSeq)
                .amount(amount)
                .fee(0L)
                .currency("KRW")
                .status(TransferStatus.FAILED)
                .failCode(FailCode.ACCOUNT_STATUS_INVALID)
                .build();
        transferRepository.save(failed);

        // 계좌 상태에 따라 적절한 ErrorCode 반환
        if (status == AccountStatus.CLOSED) {
            throw RemittanceExceptionFactory.of(ErrorCode.AMOUNT_DELETED);
        } else if (status == AccountStatus.SUSPENDED) {
            throw RemittanceExceptionFactory.of(ErrorCode.AMOUNT_SUSPENDED);
        } else {
            throw RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND);
        }
    }

    /**
     * 입금 계좌 검증용 (입금/이체 to 계좌)
     */
    private void validateCreditAccount(Account account, TransferDto.BaseReq req,
                                       Long fromAccountSeq, Long amount) {

        AccountStatus status = account.getAccountStatus();
        if (status == AccountStatus.NORMAL) {
            return;
        }

        // 실패 이력 먼저 남김
        Transfer failed = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(fromAccountSeq)
                .toAccountSeq(account.getAccountSeq())
                .amount(amount)
                .fee(0L)
                .currency("KRW")
                .status(TransferStatus.FAILED)
                .failCode(FailCode.ACCOUNT_STATUS_INVALID)
                .build();
        transferRepository.save(failed);

        if (status == AccountStatus.CLOSED) {
            throw RemittanceExceptionFactory.of(ErrorCode.AMOUNT_DELETED);
        } else if (status == AccountStatus.SUSPENDED) {
            throw RemittanceExceptionFactory.of(ErrorCode.AMOUNT_SUSPENDED);
        } else {
            throw RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND);
        }
    }

    /* ===================================================
     * 공통 유틸: 수수료 계산
     * =================================================== */

    private long calculateFee(Long amount, FeePolicy feePolicy, boolean isTransfer) {
        if (amount == null || amount <= 0 || feePolicy == null) {
            return 0L;
        }

        BigDecimal rate = isTransfer
                ? feePolicy.getTransferFeeRate()
                : feePolicy.getWithdrawFeeRate();

        if (rate == null) {
            return 0L;
        }

        return BigDecimal.valueOf(amount)
                .multiply(rate)
                .setScale(0, RoundingMode.DOWN)
                .longValueExact();
    }

    /* ===================================================
     * 1. 입금 (DEPOSIT) : 시스템 계좌 -> 고객 계좌, 수수료 없음
     *    - 요청은 accountNumber로 받고
     *    - 내부에서는 accountSeq로 전환해서 처리
     * =================================================== */

    @Transactional
    public Transfer deposit(TransferDto.DepositReq req) {

        // 1. 금액 검증
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw RemittanceExceptionFactory.of(ErrorCode.INVALID_DEPOSIT_AMOUNT);
        }

        // 2. 계좌번호로 계좌 조회 (논락)
        Account toAccountBasic = accountRepository.findByAccountNumber(req.getAccountNumber())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Long toAccountSeq = toAccountBasic.getAccountSeq();
        Long amount = req.getAmount();

        // 3. 정산 계좌 + 입금 계좌 락 (데드락 방지를 위해 작은 SEQ → 큰 SEQ 순)
        Long firstLockId = Math.min(properties.getSystemAccountSeq(), toAccountSeq);
        Long secondLockId = Math.max(properties.getSystemAccountSeq(), toAccountSeq);

        Account first = accountRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));
        Account second = accountRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Account systemAccount = first.getAccountSeq().equals(properties.getSystemAccountSeq()) ? first : second;
        Account toAccount = first.getAccountSeq().equals(toAccountSeq) ? first : second;

        // 4. 멱등성 체크 (계좌 SEQ/금액이 일치하는지 검증)
        Optional<Transfer> existingOpt = findExisting(req.getClientId(), req.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            Transfer existing = existingOpt.get();
            validateIdempotentRequest(existing,
                    properties.getSystemAccountSeq(),
                    toAccountSeq,
                    amount);
            return existing;
        }

        // 5. 입금 계좌 상태 검증
        validateCreditAccount(toAccount, req, properties.getSystemAccountSeq(), amount);

        // 6. Transfer 헤더 생성 (PENDING)
        Transfer transfer = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(properties.getSystemAccountSeq())
                .toAccountSeq(toAccountSeq)
                .amount(amount)
                .fee(0L)
                .currency("KRW")
                .status(TransferStatus.PENDING)
                .build();
        transferRepository.save(transfer);

        Long transferSeq = transfer.getTransferSeq();

        // 7. Ledger 분개 (2줄)
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(properties.getSystemAccountSeq())
                .amount(-amount)
                .entryType(EntryType.DEBIT)
                .currency("KRW")
                .build());

        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(toAccountSeq)
                .amount(amount)
                .entryType(EntryType.CREDIT)
                .currency("KRW")
                .build());

        // 8. Snapshot 업데이트
        balanceSnapshotService.applyDelta(properties.getSystemAccountSeq(), -amount);
        balanceSnapshotService.applyDelta(toAccountSeq, amount);

        // 9. 상태 POSTED로 변경
        transfer.setStatus(TransferStatus.POSTED);
        transfer.setPostedDate(LocalDateTime.now());

        return transfer;
    }

    /* ===================================================
     * 2. 출금 (WITHDRAW) : 고객 계좌 -> 시스템 계좌, 출금 수수료율 적용
     * =================================================== */

    @Transactional
    public Transfer withdraw(TransferDto.WithdrawReq req) {

        // 1. 금액 검증
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw RemittanceExceptionFactory.of(ErrorCode.INVALID_WITHDRAW_AMOUNT);
        }

        // 2. 계좌번호로 출금 계좌 조회 (논락)
        Account fromAccountBasic = accountRepository.findByAccountNumber(req.getAccountNumber())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Long fromAccountSeq = fromAccountBasic.getAccountSeq();
        Long amount = req.getAmount();

        // 3. 출금 계좌 + 시스템 계좌 락 (항상 작은 SEQ → 큰 SEQ)
        Long firstLockId = Math.min(fromAccountSeq, properties.getSystemAccountSeq());
        Long secondLockId = Math.max(fromAccountSeq, properties.getSystemAccountSeq());

        Account first = accountRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));
        Account second = accountRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Account fromAccount = first.getAccountSeq().equals(fromAccountSeq) ? first : second;
        Account systemAccount = first.getAccountSeq().equals(properties.getSystemAccountSeq()) ? first : second;

        // 4. 멱등성 체크
        Optional<Transfer> existingOpt = findExisting(req.getClientId(), req.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            Transfer existing = existingOpt.get();
            validateIdempotentRequest(existing,
                    fromAccountSeq,
                    properties.getSystemAccountSeq(),
                    amount);
            return existing;
        }

        // 5. 출금 계좌 상태 검증
        validateDebitAccount(fromAccount, req, properties.getSystemAccountSeq(), amount);

        // 6. 수수료 계산 (withdrawFeeRate)
        long fee = calculateFee(amount, fromAccount.getFeePolicy(), false);
        long totalDebit = amount + fee;

        // 7. 잔액 체크
        long fromBalance = balanceSnapshotService.getCurrentBalance(fromAccountSeq);
        if (fromBalance < totalDebit) {
            // 실패 이력 남김
            Transfer failed = Transfer.builder()
                    .clientId(req.getClientId())
                    .idempotencyKey(req.getIdempotencyKey())
                    .fromAccountSeq(fromAccountSeq)
                    .toAccountSeq(properties.getSystemAccountSeq())
                    .amount(amount)
                    .fee(fee)
                    .currency("KRW")
                    .status(TransferStatus.FAILED)
                    .failCode(FailCode.INSUFFICIENT_BALANCE)
                    .build();
            transferRepository.save(failed);

            throw RemittanceExceptionFactory.of(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 8. Transfer 헤더 생성 (PENDING)
        Transfer transfer = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(fromAccountSeq)
                .toAccountSeq(properties.getSystemAccountSeq())
                .amount(amount)
                .fee(fee)
                .currency("KRW")
                .status(TransferStatus.PENDING)
                .build();
        transferRepository.save(transfer);

        Long transferSeq = transfer.getTransferSeq();

        // 9. Ledger 분개 (3줄: 출금계좌, 시스템계좌, 수수료 계좌)
        // 9-1. 출금 계좌
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(fromAccountSeq)
                .amount(-totalDebit)
                .entryType(EntryType.DEBIT)
                .currency("KRW")
                .build());

        // 9-2. 시스템 정산 계좌
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(properties.getSystemAccountSeq())
                .amount(amount)
                .entryType(EntryType.CREDIT)
                .currency("KRW")
                .build());

        // 9-3. 수수료 수익 계좌
        if (fee > 0) {
            ledgerRepository.save(Ledger.builder()
                    .transferSeq(transferSeq)
                    .accountSeq(properties.getFeeAccountSeq())
                    .amount(fee)
                    .entryType(EntryType.CREDIT)
                    .currency("KRW")
                    .build());
        }

        // 10. Snapshot 업데이트
        balanceSnapshotService.applyDelta(fromAccountSeq, -totalDebit);
        balanceSnapshotService.applyDelta(properties.getSystemAccountSeq(), amount);
        if (fee > 0) {
            balanceSnapshotService.applyDelta(properties.getFeeAccountSeq(), fee);
        }

        // 11. 상태 POSTED로 변경
        transfer.setStatus(TransferStatus.POSTED);
        transfer.setPostedDate(LocalDateTime.now());

        return transfer;
    }

    /* ===================================================
     * 3. 계좌간 이체 (TRANSFER) : from -> to, 이체 수수료율 적용
     * =================================================== */

    @Transactional
    public Transfer transfer(TransferDto.TransferReq req) {

        // 1. 금액/계좌 검증
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw RemittanceExceptionFactory.of(ErrorCode.INVALID_AMOUNT);
        }
        if (req.getFromAccountNumber().equals(req.getToAccountNumber())) {
            throw RemittanceExceptionFactory.of(ErrorCode.TRANSFER_SAME_ACCOUNT);
        }

        // 2. 계좌번호로 양쪽 계좌 조회 (논락)
        Account fromAccountBasic = accountRepository.findByAccountNumber(req.getFromAccountNumber())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Account toAccountBasic = accountRepository.findByAccountNumber(req.getToAccountNumber())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Long fromAccountSeq = fromAccountBasic.getAccountSeq();
        Long toAccountSeq = toAccountBasic.getAccountSeq();
        Long amount = req.getAmount();

        // 3. 데드락 방지를 위해 항상 작은 번호 → 큰 번호 순으로 락
        Long firstLockId = fromAccountSeq < toAccountSeq ? fromAccountSeq : toAccountSeq;
        Long secondLockId = fromAccountSeq < toAccountSeq ? toAccountSeq : fromAccountSeq;

        Account first = accountRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));
        Account second = accountRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Account fromAccount = first.getAccountSeq().equals(fromAccountSeq) ? first : second;
        Account toAccount   = first.getAccountSeq().equals(toAccountSeq)   ? first : second;

        // 4. 멱등성 체크
        Optional<Transfer> existingOpt = findExisting(req.getClientId(), req.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            Transfer existing = existingOpt.get();
            validateIdempotentRequest(existing,
                    fromAccountSeq,
                    toAccountSeq,
                    amount);
            return existing;
        }

        // 5. 계좌 상태 검증 (실패 시 FAILED 이력 + 예외)
        validateDebitAccount(fromAccount, req, toAccountSeq, amount);
        validateCreditAccount(toAccount, req, fromAccountSeq, amount);

        // 6. 수수료 계산 (transferFeeRate)
        long fee = calculateFee(amount, fromAccount.getFeePolicy(), true);
        long totalDebit = amount + fee;

        // 7. 잔액 체크
        long fromBalance = balanceSnapshotService.getCurrentBalance(fromAccountSeq);
        if (fromBalance < totalDebit) {
            Transfer failed = Transfer.builder()
                    .clientId(req.getClientId())
                    .idempotencyKey(req.getIdempotencyKey())
                    .fromAccountSeq(fromAccountSeq)
                    .toAccountSeq(toAccountSeq)
                    .amount(amount)
                    .fee(fee)
                    .currency("KRW")
                    .status(TransferStatus.FAILED)
                    .failCode(FailCode.INSUFFICIENT_BALANCE)
                    .build();
            transferRepository.save(failed);

            throw RemittanceExceptionFactory.of(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 8. Transfer 헤더 생성 (PENDING)
        Transfer transfer = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(fromAccountSeq)
                .toAccountSeq(toAccountSeq)
                .amount(amount)
                .fee(fee)
                .currency("KRW")
                .status(TransferStatus.PENDING)
                .build();
        transferRepository.save(transfer);

        Long transferSeq = transfer.getTransferSeq();

        // 9. Ledger 분개 (3줄: 출금, 입금, 수수료)
        // 9-1. 출금 계좌
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(fromAccountSeq)
                .amount(-totalDebit)
                .entryType(EntryType.DEBIT)
                .currency("KRW")
                .build());

        // 9-2. 입금 계좌
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(toAccountSeq)
                .amount(amount)
                .entryType(EntryType.CREDIT)
                .currency("KRW")
                .build());

        // 9-3. 수수료 수익 계좌
        if (fee > 0) {
            ledgerRepository.save(Ledger.builder()
                    .transferSeq(transferSeq)
                    .accountSeq(properties.getFeeAccountSeq())
                    .amount(fee)
                    .entryType(EntryType.CREDIT)
                    .currency("KRW")
                    .build());
        }

        // 10. Snapshot 업데이트
        balanceSnapshotService.applyDelta(fromAccountSeq, -totalDebit);
        balanceSnapshotService.applyDelta(toAccountSeq, amount);
        if (fee > 0) {
            balanceSnapshotService.applyDelta(properties.getFeeAccountSeq(), fee);
        }

        // 11. 상태 POSTED로 변경
        transfer.setStatus(TransferStatus.POSTED);
        transfer.setPostedDate(LocalDateTime.now());

        return transfer;
    }
}

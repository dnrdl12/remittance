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
    
    private Optional<Transfer> findExisting(ClientId clientId, String idempotencyKey) {
        if (clientId == null || idempotencyKey == null) {
            return Optional.empty();
        }
        return transferRepository.findByClientIdAndIdempotencyKey(clientId, idempotencyKey);
    }

    // 멱등성 체크 정상여부
    private void validateIdempotentRequest(Transfer existing, Long fromAccountSeq, Long toAccountSeq, Long amount) {
        if (existing == null) return;

        boolean same = true;
        if (fromAccountSeq != null && !fromAccountSeq.equals(existing.getFromAccountSeq())) {
            same = false;
        } else if (toAccountSeq != null && !toAccountSeq.equals(existing.getToAccountSeq())) {
            same = false;
        } else if (amount != null && !amount.equals(existing.getAmount())) {
            same = false;
        }

        if (!same)
            throw RemittanceExceptionFactory.of( ErrorCode.IDEMPOTENCY_KEY_USED_DIFFERENT_PARAMS );
    }
    
    /**
     * 출금 계좌 검증용 (출금/이체 from 계좌)
     */
    private void validateDebitAccount(Account account, TransferDto.BaseReq req, Long toAccountSeq, Long amount) {
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
                .currency(properties.getDefaultCurrency())
                .status(TransferStatus.FAILED)
                .failCode(FailCode.ACCOUNT_STATUS_INVALID)
                .build();
        transferRepository.save(failed);

        // 계좌 상태 오류
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
    private void validateCreditAccount(Account account, TransferDto.BaseReq req, Long fromAccountSeq, Long amount) {

        AccountStatus status = account.getAccountStatus();
        if (status == AccountStatus.NORMAL) return;

        Transfer failed = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(fromAccountSeq)
                .toAccountSeq(account.getAccountSeq())
                .amount(amount)
                .fee(0L)
                .currency(properties.getDefaultCurrency())
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

    /** 수수료 계산 */
    private long calculateFee(Long amount, FeePolicy feePolicy, boolean isTransfer) {
        if (amount == null || amount <= 0 || feePolicy == null) return 0L;
        BigDecimal rate = isTransfer ? feePolicy.getTransferFeeRate() : feePolicy.getWithdrawFeeRate();
        if (rate == null) return 0L;
        return BigDecimal.valueOf(amount)
                .multiply(rate)
                .setScale(0, RoundingMode.DOWN)
                .longValueExact();
    }

    /**
     * 1. 입금 (DEPOSIT) : 시스템 계좌 -> 고객 계좌, 수수료 없음
     **/
    @Transactional
    public Transfer deposit(TransferDto.DepositReq req) {

        // 금액 검증
        if (req.getAmount() == null || req.getAmount() <= 0)  throw RemittanceExceptionFactory.of(ErrorCode.INVALID_DEPOSIT_AMOUNT);

        // 계좌번호로 계좌 조회
        Account toAccountBasic = accountRepository.findByAccountNumber(req.getAccountNumber())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Long toAccountSeq = toAccountBasic.getAccountSeq();
        Long amount = req.getAmount();

        Account systemAccount = accountRepository.findByIdForUpdate(properties.getSystemAccountSeq())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));
        Account toAccount = accountRepository.findByIdForUpdate(toAccountSeq)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        // 멱등성 체크
        Optional<Transfer> existingOpt = findExisting(req.getClientId(), req.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            Transfer existing = existingOpt.get();
            validateIdempotentRequest(existing, properties.getSystemAccountSeq(), toAccountSeq, amount);
            return existing;
        }

        // 입금 계좌 상태 검증
        validateCreditAccount(toAccount, req, properties.getSystemAccountSeq(), amount);

        // Transfer 생성 (PENDING)
        Transfer transfer = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(properties.getSystemAccountSeq())
                .toAccountSeq(toAccountSeq)
                .amount(amount)
                .fee(0L)
                .currency(properties.getDefaultCurrency())
                .status(TransferStatus.PENDING)
                .build();
        transferRepository.save(transfer);

        Long transferSeq = transfer.getTransferSeq();

        // Ledger 생성 (2줄)
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(properties.getSystemAccountSeq())
                .amount(-amount)
                .entryType(EntryType.DEBIT)
                .currency(properties.getDefaultCurrency())
                .build());

        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(toAccountSeq)
                .amount(amount)
                .entryType(EntryType.CREDIT)
                .currency(properties.getDefaultCurrency())
                .build());

        // 잔액 업데이트
        balanceSnapshotService.applyDelta(properties.getSystemAccountSeq(), -amount);
        balanceSnapshotService.applyDelta(toAccountSeq, amount);

        // 완료 상태 POSTED로 변경
        transfer.setStatus(TransferStatus.POSTED);
        transfer.setPostedDate(LocalDateTime.now());

        return transfer;
    }

    /**
     * 2. 출금 (WITHDRAW) : 고객 계좌 -> 시스템 계좌, 출금 수수료율 적용
     **/
    @Transactional
    public Transfer withdraw(TransferDto.WithdrawReq req) {
        // 금액확인
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw RemittanceExceptionFactory.of(ErrorCode.INVALID_WITHDRAW_AMOUNT);
        }

        // 계좌번호로 출금 계좌 조회 (논락)
        Account fromAccountBasic = accountRepository.findByAccountNumber(req.getAccountNumber())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Long fromAccountSeq = fromAccountBasic.getAccountSeq();
        Long amount = req.getAmount();

        Account systemAccount = accountRepository.findByIdForUpdate(properties.getSystemAccountSeq())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));
        Account fromAccount = accountRepository.findByIdForUpdate(fromAccountSeq)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        // 멱등성 체크
        Optional<Transfer> existingOpt = findExisting(req.getClientId(), req.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            Transfer existing = existingOpt.get();
            validateIdempotentRequest(existing, fromAccountSeq, properties.getSystemAccountSeq(), amount);
            return existing;
        }

        // 출금 계좌 상태 확이
        validateDebitAccount(fromAccount, req, properties.getSystemAccountSeq(), amount);

        // 수수료 계산 (withdrawFeeRate)
        long fee = calculateFee(amount, fromAccount.getFeePolicy(), false);
        long totalDebit = amount + fee;

        // 잔액 체크 이력남김
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
                    .currency(properties.getDefaultCurrency())
                    .status(TransferStatus.FAILED)
                    .failCode(FailCode.INSUFFICIENT_BALANCE)
                    .build();
            transferRepository.save(failed);

            throw RemittanceExceptionFactory.of(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // Transfer 생성 (PENDING)
        Transfer transfer = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(fromAccountSeq)
                .toAccountSeq(properties.getSystemAccountSeq())
                .amount(amount)
                .fee(fee)
                .currency(properties.getDefaultCurrency())
                .status(TransferStatus.PENDING)
                .build();
        transferRepository.save(transfer);
        Long transferSeq = transfer.getTransferSeq();

        // edger 생성 (3줄: 출금계좌, 시스템계좌, 수수료 계좌)
        // 출금 계좌
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(fromAccountSeq)
                .amount(-totalDebit)
                .entryType(EntryType.DEBIT)
                .currency(properties.getDefaultCurrency())
                .build());

        // 시스템 정산 계좌
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(properties.getSystemAccountSeq())
                .amount(amount)
                .entryType(EntryType.CREDIT)
                .currency(properties.getDefaultCurrency())
                .build());

        // 수수료 수익 계좌
        if (fee > 0) {
            ledgerRepository.save(Ledger.builder()
                    .transferSeq(transferSeq)
                    .accountSeq(properties.getFeeAccountSeq())
                    .amount(fee)
                    .entryType(EntryType.CREDIT)
                    .currency(properties.getDefaultCurrency())
                    .build());
        }

        // Snapshot 업데이트
        balanceSnapshotService.applyDelta(fromAccountSeq, -totalDebit);
        balanceSnapshotService.applyDelta(properties.getSystemAccountSeq(), amount);
        if (fee > 0)  balanceSnapshotService.applyDelta(properties.getFeeAccountSeq(), fee);

        // 상태 POSTED로 변경
        transfer.setStatus(TransferStatus.POSTED);
        transfer.setPostedDate(LocalDateTime.now());

        return transfer;
    }

    /**
     * 3. 계좌간 이체  : from -> to, 이체 수수료율 적용
     ***/

    @Transactional
    public Transfer transfer(TransferDto.TransferReq req) {

        // 1. 금액,계좌 검증
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw RemittanceExceptionFactory.of(ErrorCode.INVALID_AMOUNT);
        } else if (req.getFromAccountNumber().equals(req.getToAccountNumber())) {
            throw RemittanceExceptionFactory.of(ErrorCode.TRANSFER_SAME_ACCOUNT);
        }

        // 계좌번호로 양쪽 계좌 조회 (락******)
        Account fromAccountBasic = accountRepository.findByAccountNumber(req.getFromAccountNumber())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Account toAccountBasic = accountRepository.findByAccountNumber(req.getToAccountNumber())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Long fromAccountSeq = fromAccountBasic.getAccountSeq();
        Long toAccountSeq = toAccountBasic.getAccountSeq();
        Long amount = req.getAmount();

        // 데드락 방지를 위해 항상 작은 번호 → 큰 번호 순으로 락
        Long firstLockId = fromAccountSeq < toAccountSeq ? fromAccountSeq : toAccountSeq;
        Long secondLockId = fromAccountSeq < toAccountSeq ? toAccountSeq : fromAccountSeq;

        Account first = accountRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));
        Account second = accountRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        Account fromAccount = first.getAccountSeq().equals(fromAccountSeq) ? first : second;
        Account toAccount   = first.getAccountSeq().equals(toAccountSeq)   ? first : second;

        // 멱등성 체크
        Optional<Transfer> existingOpt = findExisting(req.getClientId(), req.getIdempotencyKey());
        if (existingOpt.isPresent()) {
            Transfer existing = existingOpt.get();
            validateIdempotentRequest(existing, fromAccountSeq, toAccountSeq, amount);
            return existing;
        }

        // 계좌 상태 검증 (실패 시 FAILED 이력 + 예외)
        validateDebitAccount(fromAccount, req, toAccountSeq, amount);
        validateCreditAccount(toAccount, req, fromAccountSeq, amount);

        // 수수료 계산 (transferFeeRate)
        long fee = calculateFee(amount, fromAccount.getFeePolicy(), true);
        long totalDebit = amount + fee;

        // 잔액 체크 이력남김
        long fromBalance = balanceSnapshotService.getCurrentBalance(fromAccountSeq);
        if (fromBalance < totalDebit) {
            Transfer failed = Transfer.builder()
                    .clientId(req.getClientId())
                    .idempotencyKey(req.getIdempotencyKey())
                    .fromAccountSeq(fromAccountSeq)
                    .toAccountSeq(toAccountSeq)
                    .amount(amount)
                    .fee(fee)
                    .currency(properties.getDefaultCurrency())
                    .status(TransferStatus.FAILED)
                    .failCode(FailCode.INSUFFICIENT_BALANCE)
                    .build();
            transferRepository.save(failed);

            throw RemittanceExceptionFactory.of(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // Transfer 생성 (PENDING)
        Transfer transfer = Transfer.builder()
                .clientId(req.getClientId())
                .idempotencyKey(req.getIdempotencyKey())
                .fromAccountSeq(fromAccountSeq)
                .toAccountSeq(toAccountSeq)
                .amount(amount)
                .fee(fee)
                .currency(properties.getDefaultCurrency())
                .status(TransferStatus.PENDING)
                .build();
        transferRepository.save(transfer);

        Long transferSeq = transfer.getTransferSeq();

        // Ledger 생성 (3줄: 출금, 입금, 수수료)
        // 출금 계좌
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(fromAccountSeq)
                .amount(-totalDebit)
                .entryType(EntryType.DEBIT)
                .currency(properties.getDefaultCurrency())
                .build());

        // 입금 계좌
        ledgerRepository.save(Ledger.builder()
                .transferSeq(transferSeq)
                .accountSeq(toAccountSeq)
                .amount(amount)
                .entryType(EntryType.CREDIT)
                .currency(properties.getDefaultCurrency())
                .build());

        // 수수료 수익 계좌
        if (fee > 0) {
            ledgerRepository.save(Ledger.builder()
                    .transferSeq(transferSeq)
                    .accountSeq(properties.getFeeAccountSeq())
                    .amount(fee)
                    .entryType(EntryType.CREDIT)
                    .currency(properties.getDefaultCurrency())
                    .build());
        }

        // Snapshot 업데이트
        balanceSnapshotService.applyDelta(fromAccountSeq, -totalDebit);
        balanceSnapshotService.applyDelta(toAccountSeq, amount);
        if (fee > 0) balanceSnapshotService.applyDelta(properties.getFeeAccountSeq(), fee);

        transfer.setStatus(TransferStatus.POSTED);
        transfer.setPostedDate(LocalDateTime.now());

        return transfer;
    }
}

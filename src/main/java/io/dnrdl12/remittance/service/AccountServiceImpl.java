package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.comm.api.PagingProperties;
import io.dnrdl12.remittance.comm.config.AppAccountProperties;
import io.dnrdl12.remittance.comm.enums.AccountStatus;
import io.dnrdl12.remittance.comm.enums.AccountType;
import io.dnrdl12.remittance.comm.enums.ErrorCode;
import io.dnrdl12.remittance.comm.exception.RemittanceExceptionFactory;
import io.dnrdl12.remittance.comm.utills.MaskingUtils;
import io.dnrdl12.remittance.dto.AccountDto;
import io.dnrdl12.remittance.entity.Account;
import io.dnrdl12.remittance.entity.BalanceSnapshot;
import io.dnrdl12.remittance.entity.FeePolicy;
import io.dnrdl12.remittance.entity.Member;
import io.dnrdl12.remittance.repository.AccountRepository;
import io.dnrdl12.remittance.repository.BalanceSnapshotRepository;
import io.dnrdl12.remittance.repository.FeePolicyRepository;
import io.dnrdl12.remittance.repository.MemberRepository;
import io.dnrdl12.remittance.spec.AccountSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : AccountServiceImpl
 * author         : JW.CHOI
 * date           : 2025-11-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-14        JW.CHOI              최초 생성
 * 2025-11-16        리팩터링             AccountConstants 제거, 기본값 yml+서비스로 이동
 */

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AppAccountProperties properties;
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final FeePolicyRepository feePolicyRepository;
    private final PagingProperties pagingProperties;
    private final BalanceSnapshotService balanceSnapshotService;

    @Override
    public AccountDto.Res create(AccountDto.CreateReq req, String userId) {
        Member memberRef = memberRepository.findById(req.getMemberSeq())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.USER_NOT_FOUND));

        String bankCode = Optional.ofNullable(req.getBankCode())
                .orElse(properties.getDefaultBankCode());

        String branchCode = Optional.ofNullable(req.getBranchCode())
                .orElse(properties.getDefaultBranchCode());

        Long dailyTransferLimit = Optional.ofNullable(req.getDailyTransferLimit())
                .orElse(properties.getDefaultDailyTransferLimit());

        Long dailyWithdrawLimit = Optional.ofNullable(req.getDailyWithdrawLimit())
                .orElse(properties.getDefaultDailyWithdrawLimit());

        if (req.getFeePolicySeq() == null) {
            throw RemittanceExceptionFactory.of(ErrorCode.FEE_POLICY_NOT_SELECT);
        }

        FeePolicy feePolicyRef = feePolicyRepository.findById(req.getFeePolicySeq())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.FEE_POLICY_NOT_FOUND));

        AccountType accountType = Optional.ofNullable(req.getAccountType())
                .orElse(AccountType.NORMAL);

        Account account = Account.builder()
                .member(memberRef)
                .accountNumber(generateAccountNumber())
                .nickname(req.getNickname())
                .accountType(accountType)
                .bankCode(bankCode)
                .branchCode(branchCode)
                .feePolicy(feePolicyRef)
                .dailyTransferLimit(dailyTransferLimit)
                .dailyWithdrawLimit(dailyWithdrawLimit)
                .build();

        account.setRegId(userId);
        account.setModId(userId);

        Account saved = accountRepository.save(account);
        balanceSnapshotService.initForAccount(saved);
        return toRes(saved);
    }

    @Override
    public Page<AccountDto.SearchSimpleRes> searchAccounts(AccountDto.SearchReq req, boolean masked) {
        Pageable pageable = PageRequest.of(
                req.getPage() != null ? req.getPage() : 0,
                req.getSize() != null ? Math.min(req.getSize(), pagingProperties.maxSize()) : pagingProperties.defaultSize(),
                Sort.by(Sort.Direction.DESC, "accountSeq")
        );
        Page<Account> page = accountRepository.findAll(AccountSpec.search(req), pageable);
        return page.map(a -> toSearchSimpleRes(a, masked));
    }

    @Override
    public AccountDto.SearchDetailRes getAccountDetail(Long accountSeq) {
        Account account = accountRepository.findByAccountSeq(accountSeq)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));
        return toSearchDetailRes(account);
    }

    private AccountDto.SearchSimpleRes toSearchSimpleRes(Account account, boolean masked) {
        Member m = account.getMember();

        String phone = m.getMemberPhone();
        if (masked) {
            phone = MaskingUtils.maskPhone(phone);
        }

        return AccountDto.SearchSimpleRes.builder()
                .accountSeq(account.getAccountSeq())
                .accountNumber(account.getAccountNumber())
                .nickname(account.getNickname())
                .accountStatus(account.getAccountStatus())
                .accountType(account.getAccountType())
                .bankCode(account.getBankCode())
                .branchCode(account.getBranchCode())
                .memberNm(m.getMemberNm())
                .memberPhone(phone)
                .memberStatus(m.getMemberStatus())
                .privConsentYn(m.getPrivConsentYn())
                .msgConsentYn(m.getMsgConsentYn())
                .build();
    }

    private AccountDto.SearchDetailRes toSearchDetailRes(Account account) {
        Member m = account.getMember();
        BalanceSnapshot b = account.getBalanceSnapshot();
        FeePolicy f = account.getFeePolicy();

        return AccountDto.SearchDetailRes.builder()
                .accountSeq(account.getAccountSeq())
                .accountNumber(account.getAccountNumber())
                .nickname(account.getNickname())
                .accountStatus(account.getAccountStatus())
                .accountType(account.getAccountType())
                .bankCode(account.getBankCode())
                .branchCode(account.getBranchCode())
                .createdDate(account.getCreatedDate())
                .memberNm(m != null ? m.getMemberNm() : null)
                .memberPhone(m != null ? m.getMemberPhone() : null)
                .memberStatus(m != null ? m.getMemberStatus() : null)
                .privConsentYn(m != null ? m.getPrivConsentYn() : null)
                .msgConsentYn(m != null ? m.getMsgConsentYn() : null)
                .balance(b != null ? b.getBalance() : null)
                .policyName(f != null ? f.getPolicyName() : null)
                .transferFeeRate(f != null ? f.getTransferFeeRate() : null)
                .withdrawFeeRate(f != null ? f.getWithdrawFeeRate() : null)
                .build();
    }

    @Override
    public AccountDto.Res patch(Long accountSeq, AccountDto.PatchReq req, String userId) {
        Account account = accountRepository.findById(accountSeq)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        if (req.getNickname() != null) {
            account.setNickname(req.getNickname());
        }

        AccountStatus status = req.toAccountStatusOrNull();
        if (status != null) {
            account.setAccountStatus(status);
        }

        account.setModId(userId);
        return toRes(account);
    }

    @Override
    @Transactional
    public AccountDto.IdResponse deleteSoft(Long accountSeq, String userId) {
        Account account = accountRepository.findById(accountSeq)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.ACCOUNT_NOT_FOUND));

        if (AccountStatus.CLOSED.getCode().equals(account.getAccountStatus().getCode())) {
            throw RemittanceExceptionFactory.of(ErrorCode.AMOUNT_ALREADY_DELETED);
        }

        account.setAccountStatus(AccountStatus.CLOSED); // 해지
        account.setClosedDate(LocalDateTime.now());
        account.setModId(userId);
        return AccountDto.IdResponse.of(account.getAccountSeq());
    }

    private AccountDto.Res toRes(Account a) {
        Long balance = balanceSnapshotRepository.findById(a.getAccountSeq())
                .map(BalanceSnapshot::getBalance)
                .orElse(0L);

        return AccountDto.Res.builder()
                .accountSeq(a.getAccountSeq())
                .accountNumber(a.getAccountNumber())
                .memberSeq(a.getMember().getMemberSeq())
                .nickname(a.getNickname())
                .balance(balance)
                .accountStatus(a.getAccountStatus())
                .createdDate(a.getCreatedDate())
                .build();
    }

    /**
     * 계좌번호 생성 로직 (예시용)
     */
    private String generateAccountNumber() {
        long num = (long) (Math.random() * 1_0000_0000_0000L) + 1_0000_0000_0000L;
        return String.valueOf(num);
    }
}

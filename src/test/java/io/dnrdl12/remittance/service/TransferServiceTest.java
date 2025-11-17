package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.comm.config.AppAccountProperties;
import io.dnrdl12.remittance.comm.enums.*;
import io.dnrdl12.remittance.comm.exception.RemittanceException;
import io.dnrdl12.remittance.comm.exception.RemittanceExceptionFactory;
import io.dnrdl12.remittance.dto.TransferDto;
import io.dnrdl12.remittance.entity.*;
import io.dnrdl12.remittance.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : AccountServiceTest
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI            최초 생성
 * 2025-11-17        JW.CHOI            불필요한 소스제거
 */
@SpringBootTest
@Transactional
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FeePolicyRepository feePolicyRepository;

    @Autowired
    private BalanceSnapshotRepository balanceSnapshotRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private AppAccountProperties appAccountProperties;
    @Autowired
    private AppAccountProperties properties;

    // ------------------------------
    // 기본 시스템 계좌 (properties 기반)
    // ------------------------------
    private Long systemAccountSeq;
    private Long feeRevenueAccountSeq;

    // 테스트용 계좌
    private Long userAccount1Seq;
    private Long userAccount2Seq;
    private final String userAccount1Number = "777-0000-000001";
    private final String userAccount2Number = "777-0000-000002";


    @TestConfiguration
    static class TestBuildPropertiesConfig {
        @Bean
        public BuildProperties buildProperties() {
            Properties props = new Properties();
            props.setProperty("name", "remittance-test");
            props.setProperty("version", "0.0.1-TEST");
            return new BuildProperties(props);
        }
    }

    @BeforeEach
    void setUp() {
        // ---------------------------------------------
        // 0) 시스템 계좌 seq는 설정에서 가져옴
        // ---------------------------------------------
        systemAccountSeq = appAccountProperties.getSystemAccountSeq();
        feeRevenueAccountSeq = appAccountProperties.getFeeAccountSeq();

        // ---------------------------------------------
        // 1) 시스템/수수료 계좌는 초기 데이터에서 존재
        //    (없으면 테스트 환경 문제)
        // ---------------------------------------------
        Account systemAccount = accountRepository.findById(systemAccountSeq)
                .orElseThrow(() -> new RuntimeException("SYSTEM ACCOUNT MISSING"));
        Account feeAccount = accountRepository.findById(feeRevenueAccountSeq)
                .orElseThrow(() -> new RuntimeException("FEE ACCOUNT MISSING"));

        // ---------------------------------------------
        // 2) 수수료 정책도 이미 3개 존재
        //    기본 수수료 정책 PK = 1이라고 가정
        // ---------------------------------------------
        FeePolicy basePolicy = feePolicyRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("BASE FEE POLICY MISSING"));


        Member userMember1 = Member.builder()
                .memberNm("테스트회원1")
                .memberPhone("01011112222")
                .memberCi("CI-USER1")
                .memberDi("DI-USER1")
                .memberStatus(MemberStatus.ACTIVE)   // enum 이름은 프로젝트에 맞게
                .privConsentYn("Y")
                .msgConsentYn("Y")
                .build();

        Member userMember2 = Member.builder()
                .memberNm("테스트회원2")
                .memberPhone("01022223333")
                .memberCi("CI-USER2")
                .memberDi("DI-USER2")
                .memberStatus(MemberStatus.ACTIVE)
                .privConsentYn("Y")
                .msgConsentYn("Y")
                .build();

        memberRepository.save(userMember1);
        memberRepository.save(userMember2);

        // ---------------------------------------------
        // 4) 테스트용 계좌 2개 생성 (각기 다른 회원에 매핑)
        // ---------------------------------------------
        String bankCode = properties.getDefaultBankCode();
        String branchCode = properties.getDefaultBranchCode();
        Long dailyTransferLimit = properties.getDefaultDailyTransferLimit();
        Long dailyWithdrawLimit = properties.getDefaultDailyWithdrawLimit();
        AccountType accountType = AccountType.NORMAL;

        Account user1 = Account.builder()
                .accountNumber(userAccount1Number)
                .member(userMember1)
                .accountStatus(AccountStatus.NORMAL)
                .accountType(accountType)
                .feePolicy(basePolicy)
                .bankCode(bankCode)
                .branchCode(branchCode)
                .dailyTransferLimit(dailyTransferLimit)
                .dailyWithdrawLimit(dailyWithdrawLimit)
                .build();

        Account user2 = Account.builder()
                .accountNumber(userAccount2Number)
                .member(userMember2)
                .accountStatus(AccountStatus.NORMAL)
                .accountType(accountType)
                .feePolicy(basePolicy)
                .bankCode(bankCode)
                .branchCode(branchCode)
                .dailyTransferLimit(dailyTransferLimit)
                .dailyWithdrawLimit(dailyWithdrawLimit)
                .build();

        accountRepository.save(user1);
        accountRepository.save(user2);

        userAccount1Seq = user1.getAccountSeq();
        userAccount2Seq = user2.getAccountSeq();

        // ---------------------------------------------
        // 5) 스냅샷 초기화 – 모두 0으로 세팅
        // ---------------------------------------------
        initSnapshot(systemAccountSeq, 0L);
        initSnapshot(feeRevenueAccountSeq, 0L);
        initSnapshot(userAccount1Seq, 0L);
        initSnapshot(userAccount2Seq, 0L);
    }

    private void initSnapshot(Long accountSeq, Long balance) {
        Optional<BalanceSnapshot> existingOpt = balanceSnapshotRepository.findById(accountSeq);

        if (existingOpt.isPresent()) {
            BalanceSnapshot snapshot = existingOpt.get();
            snapshot.setBalance(balance);
            balanceSnapshotRepository.save(snapshot);
            return;
        }
        Account account = accountRepository.findById(accountSeq)
                .orElseThrow(() -> new IllegalStateException("Account not found for snapshot: " + accountSeq));

        BalanceSnapshot snapshot = BalanceSnapshot.builder()
                .account(account)
                .balance(balance)
                .build();

        balanceSnapshotRepository.save(snapshot);
    }

    private long getSnapshotBalance(Long accountSeq) {
        return balanceSnapshotRepository.findById(accountSeq)
                .map(BalanceSnapshot::getBalance)
                .orElse(0L);
    }

    // -------------------------------------------------------------
    // TEST 1: 입금 성공
    // -------------------------------------------------------------
    @Test
    @DisplayName("입금 성공 시 Transfer=POSTED, ledger/snapshot 정상 반영")
    void deposit_success() {
        long amount = 100_000L;

        TransferDto.DepositReq req = TransferDto.DepositReq.builder()
                .clientId(ClientId.WEB)
                .idempotencyKey("TEST-DEPOSIT-001")
                .accountNumber(userAccount1Number)
                .amount(amount)
                .build();

        Transfer t = transferService.deposit(req);

        assertThat(t.getStatus()).isEqualTo(TransferStatus.POSTED);
        assertThat(t.getAmount()).isEqualTo(amount);
        assertThat(t.getFee()).isEqualTo(0L);

        // snapshot: 시스템 -100_000, user1 +100_000
        assertThat(getSnapshotBalance(systemAccountSeq)).isEqualTo(-amount);
        assertThat(getSnapshotBalance(userAccount1Seq)).isEqualTo(amount);
    }

    // -------------------------------------------------------------
    // TEST 2: 출금 잔액 부족
    // -------------------------------------------------------------
    @Test
    @DisplayName("출금 - 잔액 부족 시 FAILED + 예외 발생")
    void withdraw_insufficientBalance() {
        TransferDto.WithdrawReq req = TransferDto.WithdrawReq.builder()
                .clientId(ClientId.WEB)
                .idempotencyKey("TEST-WITHDRAW-001")
                .accountNumber(userAccount1Number)
                .amount(50_000L)
                .build();

        assertThatThrownBy(() -> transferService.withdraw(req))
                .isInstanceOf(RemittanceException.class);

        // FAILED 기록 확인
        assertThat(transferRepository.findAll())
                .anyMatch(t -> t.getStatus() == TransferStatus.FAILED
                        && t.getFailCode() == FailCode.INSUFFICIENT_BALANCE);
    }

    // -------------------------------------------------------------
    // TEST 3: 계좌간 이체 성공
    // -------------------------------------------------------------
    @Test
    @DisplayName("이체 성공 시 출금/입금/수수료 및 snapshot 정상 반영")
    void transfer_success() {
        // 1) 먼저 user1에 200,000 입금
        TransferDto.DepositReq dep = TransferDto.DepositReq.builder()
                .clientId(ClientId.WEB)
                .idempotencyKey("DEP-USER1-200K")
                .accountNumber(userAccount1Number)
                .amount(200_000L)
                .build();
        transferService.deposit(dep);

        // 2) 100,000 이체 → 수수료 0.1% = 100원 (기본 정책 기준)
        TransferDto.TransferReq req = TransferDto.TransferReq.builder()
                .clientId(ClientId.WEB)
                .idempotencyKey("TRANSFER-100K")
                .fromAccountNumber(userAccount1Number)
                .toAccountNumber(userAccount2Number)
                .amount(100_000L)
                .build();

        Transfer t = transferService.transfer(req);

        assertThat(t.getStatus()).isEqualTo(TransferStatus.POSTED);
        assertThat(t.getFee()).isEqualTo(100L);

        // user1: 200,000 → -100,000 -100 = 99,900
        assertThat(getSnapshotBalance(userAccount1Seq)).isEqualTo(99_900L);

        // user2: +100,000
        assertThat(getSnapshotBalance(userAccount2Seq)).isEqualTo(100_000L);

        // fee 계좌: +100
        assertThat(getSnapshotBalance(feeRevenueAccountSeq)).isEqualTo(100L);
    }

    // -------------------------------------------------------------
    // TEST 4: 정지 계좌 출금
    // -------------------------------------------------------------
    @Test
    @DisplayName("정지(SUSPENDED) 계좌 출금 시 FAILED + 예외")
    void withdraw_suspended() {
        Account user1 = accountRepository.findById(userAccount1Seq)
                .orElseThrow();
        user1.setAccountStatus(AccountStatus.SUSPENDED);
        // flush 없어도 같은 트랜잭션 안이라 dirty checking 됨

        TransferDto.WithdrawReq req = TransferDto.WithdrawReq.builder()
                .clientId(ClientId.WEB)
                .idempotencyKey("WITHDRAW-SUSP")
                .accountNumber(userAccount1Number)
                .amount(10_000L)
                .build();

        assertThatThrownBy(() -> transferService.withdraw(req))
                .isInstanceOf(RemittanceException.class);

        assertThat(transferRepository.findAll())
                .anyMatch(t -> t.getStatus() == TransferStatus.FAILED
                        && t.getFailCode() == FailCode.ACCOUNT_STATUS_INVALID);
    }
}

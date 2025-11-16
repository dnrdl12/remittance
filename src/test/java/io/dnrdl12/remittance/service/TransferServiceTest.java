package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.comm.enums.*;
import io.dnrdl12.remittance.comm.exception.RemittanceException;
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

import java.math.BigDecimal;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

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

    // ------------------------------
    // 기본 시스템 계좌(PK = 1, 2)
    // ------------------------------
    private Long systemAccountSeq = 1L;
    private Long feeRevenueAccountSeq = 2L;
    private String systemAccountNumber = "999-0000-000001";
    private String feeAccountNumber    = "999-0000-000002";

    // 테스트용 계좌
    private Long userAccount1Seq;
    private Long userAccount2Seq;
    private String userAccount1Number = "777-0000-000001";
    private String userAccount2Number = "777-0000-000002";

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
        // 1) 시스템 계좌는 이미 초기 데이터에서 들어있음
        // ---------------------------------------------
        Account systemAccount = accountRepository.findById(systemAccountSeq)
                .orElseThrow(() -> new RuntimeException("SYSTEM ACCOUNT MISSING"));
        Account feeAccount = accountRepository.findById(feeRevenueAccountSeq)
                .orElseThrow(() -> new RuntimeException("FEE ACCOUNT MISSING"));

        // ---------------------------------------------
        // 2) 수수료 정책도 이미 3개 존재
        // 기본 수수료 정책 id = 1
        // VIP_FEE           id = 2
        // BANK_FEE          id = 3
        // ---------------------------------------------
        FeePolicy basePolicy = feePolicyRepository.findById(1L).orElseThrow();

        // ---------------------------------------------
        // 3) 테스트용 계좌 2개 생성 (초기 DB에는 없음)
        // ---------------------------------------------
        Member systemMember = memberRepository.findById(1L).orElseThrow(); // SYSTEM MEMBER

        Account user1 = Account.builder()
                .accountNumber(userAccount1Number)
                .member(systemMember)
                .accountStatus(AccountStatus.NORMAL)
                .accountType(AccountType.NORMAL)
                .feePolicy(basePolicy)
                .build();

        Account user2 = Account.builder()
                .accountNumber(userAccount2Number)
                .member(systemMember)
                .accountStatus(AccountStatus.NORMAL)
                .accountType(AccountType.NORMAL)
                .feePolicy(basePolicy)
                .build();

        accountRepository.save(user1);
        accountRepository.save(user2);

        userAccount1Seq = user1.getAccountSeq();
        userAccount2Seq = user2.getAccountSeq();

        // ---------------------------------------------
        // 4) 테스트 계좌의 초기 잔액 스냅샷 추가
        // 시스템 계좌 잔액은 이미 존재하므로 skip
        // ---------------------------------------------
        initSnapshotIfNotExist(userAccount1Seq, 0L);
        initSnapshotIfNotExist(userAccount2Seq, 0L);
    }

    private void initSnapshotIfNotExist(Long accountSeq, Long balance) {
        if (balanceSnapshotRepository.findById(accountSeq).isEmpty()) {
            BalanceSnapshot snapshot = BalanceSnapshot.builder()
                    .accountSeq(accountSeq)
                    .balance(balance)
                    .build();
            balanceSnapshotRepository.save(snapshot);
        }
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
    @DisplayName("입금 성공 시 Post/ledger/snapshot 정상")
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

        // snapshot
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
        assertThat(
                transferRepository.findAll()
        ).anyMatch(t -> t.getStatus() == TransferStatus.FAILED
                && t.getFailCode() == FailCode.INSUFFICIENT_BALANCE);
    }

    // -------------------------------------------------------------
    // TEST 3: 계좌간 이체 성공
    // -------------------------------------------------------------
    @Test
    @DisplayName("이체 성공 시 출금/입금/수수료 반영")
    void transfer_success() {
        // 1) 먼저 user1에 200,000 입금
        TransferDto.DepositReq dep = TransferDto.DepositReq.builder()
                .clientId(ClientId.WEB)
                .idempotencyKey("DEP-USER1-200K")
                .accountNumber(userAccount1Number)
                .amount(200_000L)
                .build();
        transferService.deposit(dep);

        // 2) 100,000 이체 → 수수료 0.1% = 100원
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

        Account user1 = accountRepository.findById(userAccount1Seq).orElseThrow();
        user1.setAccountStatus(AccountStatus.SUSPENDED);

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

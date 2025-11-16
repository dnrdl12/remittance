package io.dnrdl12.remittance.comm.config;


import io.dnrdl12.remittance.comm.enums.AccountStatus;
import io.dnrdl12.remittance.comm.enums.AccountType;
import io.dnrdl12.remittance.comm.enums.MemberStatus;
import io.dnrdl12.remittance.dto.TransferDto;
import io.dnrdl12.remittance.entity.Account;
import io.dnrdl12.remittance.entity.BalanceSnapshot;
import io.dnrdl12.remittance.entity.FeePolicy;
import io.dnrdl12.remittance.entity.Member;
import io.dnrdl12.remittance.repository.AccountRepository;
import io.dnrdl12.remittance.repository.BalanceSnapshotRepository;
import io.dnrdl12.remittance.repository.MemberRepository;
import io.dnrdl12.remittance.service.TransferService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * packageName    : io.dnrdl12.remittance.comm.config
 * fileName       : DataInitConfig
 * author         : JW.CHOI
 * date           : 2025-11-14
 * description    : 개발서버 시작 시 초기 데이터 생성
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-14        JW.CHOI              최초 생성
 */
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataInitConfig  implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final TransferService transferService;

    @Override
    @Transactional
    public void run(String... args) {
        /*
        initMembers();
        initAccounts();

        // 이미 스냅샷이 있다면(테스트 재실행 등) 초기 입금 스킵
        if (balanceSnapshotRepository.count() == 0) {
            initBalancesWithDeposit();
        }

         */
    }

    private void initMembers() {
        if (memberRepository.count() > 0) return;

        List<Member> dummyMembers = List.of(
                createMember("최정욱", "01011112222", "CI-001", "DI-001"),
                createMember("최진후", "01022223333", "CI-002", "DI-002"),
                createMember("이영희", "01033334444", "CI-003", "DI-003"),
                createMember("박민수", "01044445555", "CI-004", "DI-004"),
                createMember("최지우", "01055556666", "CI-005", "DI-005")
        );
        memberRepository.saveAll(dummyMembers);
    }

    private void initAccounts() {
        if (accountRepository.count() > 0) return;

        List<Member> members = memberRepository.findAll();

        Member m1 = findByName(members, "최정욱");
        Member m2 = findByName(members, "최진후");
        Member m3 = findByName(members, "이영희");
        Member m4 = findByName(members, "박민수");
        Member m5 = findByName(members, "최지우");

        Account a1 = createAccount(m1, "111-111-111", "정욱-메인", 1L);
        Account a2 = createAccount(m1, "111-111-222", "정욱-세컨드", 1L);
        Account a3 = createAccount(m2, "222-222-222", "진후-계좌", 1L);
        Account a4 = createAccount(m3, "333-333-333", "영희-계좌", 1L);
        Account a5 = createAccount(m4, "444-444-444", "민수-계좌", 1L);
        Account a6 = createAccount(m5, "555-555-555", "지우-계좌", 1L);

        accountRepository.saveAll(List.of(a1, a2, a3, a4, a5, a6));
    }

    /**
     * 초기 잔액을 "입금 API 비즈니스 로직"을 통해 생성
     * - 111-111-111 : 1,000,000
     * - 111-111-222 :   500,000
     * - 나머지      :   300,000
     */
    private void initBalancesWithDeposit() {
        List<Account> accounts = accountRepository.findAll();

        for (Account account : accounts) {
            long initBalance;
            String accNo = account.getAccountNumber();

            if ("111-111-111".equals(accNo)) {
                initBalance = 1_000_000L;
            } else if ("111-111-222".equals(accNo)) {
                initBalance = 500_000L;
            } else {
                initBalance = 300_000L;
            }

            if (initBalance <= 0) continue;

            TransferDto.DepositReq req = TransferDto.DepositReq.builder()
                    .accountNumber(accNo)
                    .amount(initBalance)
                    .build();

            transferService.deposit(req);
        }
    }

    // ====== 헬퍼들 ======

    private Member createMember(String name, String phone, String ci, String di) {
        Member m = Member.builder()
                .memberNm(name)
                .memberPhone(phone)
                .memberCi(ci)
                .memberDi(di)
                .memberStatus(MemberStatus.ACTIVE)
                .privConsentYn("Y")
                .msgConsentYn("Y")
                .build();
        m.setRegId("init");
        return m;
    }

    private Account createAccount(Member member, String accountNumber, String nickname, Long feePolicySeq) {
        Account m = Account.builder()
                .accountNumber(accountNumber)
                .member(member)           // 연관관계 버전
                // .memberSeq(member.getMemberSeq()) // member_seq 필드 방식이면 이걸로
                .nickname(nickname)
                .accountStatus(AccountStatus.NORMAL)
                .accountType(AccountType.NORMAL)
                .feePolicy(FeePolicy.builder().feePolicySeq(feePolicySeq).build())
                .build();
        m.setRegId("init");
        return m;
    }

    private Member findByName(List<Member> members, String name) {
        return members.stream()
                .filter(m -> name.equals(m.getMemberNm()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("기초 데이터에 [" + name + "] 회원이 없습니다."));
    }
}
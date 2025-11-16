package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.comm.config.AppAccountProperties;
import io.dnrdl12.remittance.comm.enums.AccountStatus;
import io.dnrdl12.remittance.comm.enums.AccountType;
import io.dnrdl12.remittance.comm.enums.ErrorCode;
import io.dnrdl12.remittance.comm.exception.RemittanceException;
import io.dnrdl12.remittance.dto.AccountDto;
import io.dnrdl12.remittance.entity.Account;
import io.dnrdl12.remittance.entity.FeePolicy;
import io.dnrdl12.remittance.entity.Member;
import io.dnrdl12.remittance.repository.AccountRepository;
import io.dnrdl12.remittance.repository.FeePolicyRepository;
import io.dnrdl12.remittance.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

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
 */
@SpringBootTest
@Transactional
class AccountServiceTest {

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FeePolicyRepository feePolicyRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AppAccountProperties accountProps;

    // balanceSnapshotService 는 create() 내부에서 호출되지만,
    // 여기서는 동작 자체가 중요하지 않으므로 Mock 으로 막아둔다.
    @MockBean
    private BalanceSnapshotService balanceSnapshotService;

    private Member member;
    private FeePolicy feePolicy;

    @BeforeEach
    void setUp() {
        // 1) 테스트용 Member 생성 (NOT NULL 컬럼 고려)
        member = Member.builder()
                .memberNm("테스트회원")
                .memberPhone("01012345678")
                .memberPhoneHash("HASH-PHONE")
                .memberCi("CI-TEST")
                .memberCiHash("HASH-CI")
                .memberDi("DI-TEST")
                .memberDiHash("HASH-DI")
                .memberStatus(io.dnrdl12.remittance.comm.enums.MemberStatus.ACTIVE)
                .msgConsentYn("Y")
                .privConsentYn("Y")
                .build();
        memberRepository.save(member);

        // 2) Flyway V2__init_data.sql 로 들어간 fee_policy 중 하나 사용
        //    (초기 데이터가 있다는 전제를 활용)
        this.feePolicy = feePolicyRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("초기 fee_policy 데이터가 없습니다. V2__init_data.sql 을 확인하세요."));
    }

    @Test
    @DisplayName("계좌 생성 시 요청값이 null 이면 yml 기본값이 들어간다")
    void createAccount_usesDefaultsFromProperties() {
        // given
        AccountDto.CreateReq req = AccountDto.CreateReq.builder()
                .memberSeq(member.getMemberSeq())
                .nickname("테스트계좌")
                // bankCode, branchCode, daily limits 전부 null → 기본값 사용
                .feePolicySeq(feePolicy.getFeePolicySeq())
                .build();

        String userId = "tester";

        // when
        AccountDto.Res res = accountService.create(req, userId);

        // then
        Account saved = accountRepository.findById(res.getAccountSeq())
                .orElseThrow();

        assertThat(saved.getMember().getMemberSeq()).isEqualTo(member.getMemberSeq());
        assertThat(saved.getNickname()).isEqualTo("테스트계좌");

        // yml 에서 읽은 기본값과 동일해야 함
        assertThat(saved.getBankCode()).isEqualTo(accountProps.getDefaultBankCode());
        assertThat(saved.getBranchCode()).isEqualTo(accountProps.getDefaultBranchCode());
        assertThat(saved.getDailyTransferLimit()).isEqualTo(accountProps.getDefaultDailyTransferLimit());
        assertThat(saved.getDailyWithdrawLimit()).isEqualTo(accountProps.getDefaultDailyWithdrawLimit());

        // AccountType 이 null 이었으므로 NORMAL 기본값이어야 함
        assertThat(saved.getAccountType()).isEqualTo(AccountType.NORMAL);

        // 상태는 NORMAL
        assertThat(saved.getAccountStatus()).isEqualTo(AccountStatus.NORMAL);
    }

    @Test
    @DisplayName("계좌 생성 시 feePolicySeq 가 없으면 예외가 발생한다")
    void createAccount_withoutFeePolicy_throwsException() {
        // given
        AccountDto.CreateReq req = AccountDto.CreateReq.builder()
                .memberSeq(member.getMemberSeq())
                .nickname("테스트계좌")
                // feePolicySeq 누락
                .build();

        // when & then
        assertThatThrownBy(() -> accountService.create(req, "tester"))
                .isInstanceOf(RemittanceException.class)
                .satisfies(ex -> {
                    RemittanceException re = (RemittanceException) ex;
                    assertThat(re.getErrorCode()).isEqualTo(ErrorCode.FEE_POLICY_NOT_SELECT);
                });
    }

    @Test
    @DisplayName("계좌 정보 수정(patch) 시 닉네임과 상태가 정상적으로 변경된다")
    void patchAccount_success() {
        // given: 먼저 계좌 하나 생성
        AccountDto.CreateReq createReq = AccountDto.CreateReq.builder()
                .memberSeq(member.getMemberSeq())
                .nickname("원래이름")
                .accountType(AccountType.NORMAL)
                .feePolicySeq(feePolicy.getFeePolicySeq())
                .build();
        AccountDto.Res created = accountService.create(createReq, "tester");
        Long accountSeq = created.getAccountSeq();

        AccountDto.PatchReq patchReq = AccountDto.PatchReq.builder()
                .nickname("새이름")
                .accountStatusCode(AccountStatus.SUSPENDED.getCode()) // 예: int code 로 받는다면
                .build();

        // when
        AccountDto.Res patched = accountService.patch(accountSeq, patchReq, "admin");

        // then
        Account updated = accountRepository.findById(accountSeq).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("새이름");
        assertThat(updated.getAccountStatus()).isEqualTo(AccountStatus.SUSPENDED);
    }

    @Test
    @DisplayName("계좌 해지(deleteSoft) 시 상태가 CLOSED 로 변경되고 해지일이 들어간다")
    void deleteSoft_closesAccount() {
        // given: 먼저 계좌 생성
        AccountDto.CreateReq createReq = AccountDto.CreateReq.builder()
                .memberSeq(member.getMemberSeq())
                .nickname("삭제테스트")
                .accountType(AccountType.NORMAL)
                .feePolicySeq(feePolicy.getFeePolicySeq())
                .build();
        AccountDto.Res created = accountService.create(createReq, "tester");
        Long accountSeq = created.getAccountSeq();

        // when
        AccountDto.IdResponse idRes = accountService.deleteSoft(accountSeq, "admin");

        // then
        Account closed = accountRepository.findById(idRes.getAccountSeq()).orElseThrow();
        assertThat(closed.getAccountStatus()).isEqualTo(AccountStatus.CLOSED);
        assertThat(closed.getClosedDate()).isNotNull();
    }

    @Test
    @DisplayName("계좌 검색(searchAccounts) 시 페이지 결과가 반환된다")
    void searchAccounts_returnsPage() {
        // given: 테스트용 멤버 계좌 2개 생성
        for (int i = 0; i < 2; i++) {
            AccountDto.CreateReq createReq = AccountDto.CreateReq.builder()
                    .memberSeq(member.getMemberSeq())
                    .nickname("계좌" + i)
                    .accountType(AccountType.NORMAL)
                    .feePolicySeq(feePolicy.getFeePolicySeq())
                    .build();
            accountService.create(createReq, "tester");
        }

        AccountDto.SearchReq searchReq = new AccountDto.SearchReq();
        searchReq.setPage(0);
        searchReq.setSize(10);
        // ⚠️ 시스템 계좌들이 섞이지 않게, 테스트 회원 이름으로 필터링
        searchReq.setMemberNm("테스트회원");

        // when
        Page<AccountDto.SearchSimpleRes> page = accountService.searchAccounts(searchReq, true);

        // then
        assertThat(page.getContent()).isNotEmpty();

        // 마스킹 여부만 간단 체크
        AccountDto.SearchSimpleRes first = page.getContent().get(0);
        assertThat(first.getMemberPhone()).contains("*"); // MaskingUtils.maskPhone 결과 형태에 맞게 조정 가능
    }
}

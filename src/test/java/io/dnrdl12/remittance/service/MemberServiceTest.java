package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.comm.api.PageDto;
import io.dnrdl12.remittance.comm.enums.ErrorCode;
import io.dnrdl12.remittance.comm.enums.MemberStatus;
import io.dnrdl12.remittance.comm.exception.RemittanceException;
import io.dnrdl12.remittance.comm.crypto.CryptoUtil;
import io.dnrdl12.remittance.dto.MemberDto;
import io.dnrdl12.remittance.entity.Member;
import io.dnrdl12.remittance.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static io.dnrdl12.remittance.comm.utills.StringUtils.normalizePhone;
import static org.assertj.core.api.Assertions.*;

/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : MemberServiceTest
 * author         : JW.CHOI
 * date           : 2025-11-17
 * description    : MemberServiceImpl 통합 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-17        JW.CHOI            최초 생성
 */
@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberServiceImpl memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CryptoUtil cryptoUtil;

    private final String creatorId = "tester";

    @BeforeEach
    void setUp() {
        // 특별히 선행 데이터는 필요 없음
    }

    @Test
    @DisplayName("회원 생성 시 전화번호/CI/DI 해시가 저장되고 기본 동의값이 세팅된다")
    void createMember_success() {
        // given
        MemberDto.CreateReq req = MemberDto.CreateReq.builder()
                .memberNm("홍길동")
                .memberPhone("010-1234-5678")
                .memberCi("CI-001")
                .memberDi("DI-001")
                // privConsentYn, msgConsentYn → null 이면 기본 'Y'
                .build();

        // when
        MemberDto.IdResponse idRes = memberService.create(req, creatorId);

        // then
        Member saved = memberRepository.findById(idRes.getMemberSeq())
                .orElseThrow();

        String normalizedPhone = normalizePhone("010-1234-5678");
        String expectedPhoneHash = cryptoUtil.hmacHash(normalizedPhone);
        String expectedCiHash = cryptoUtil.hmacHash("CI-001");
        String expectedDiHash = cryptoUtil.hmacHash("DI-001");

        assertThat(saved.getMemberNm()).isEqualTo("홍길동");
        assertThat(saved.getMemberPhone()).isEqualTo(normalizedPhone);
        assertThat(saved.getMemberPhoneHash()).isEqualTo(expectedPhoneHash);
        assertThat(saved.getMemberCi()).isEqualTo("CI-001");
        assertThat(saved.getMemberCiHash()).isEqualTo(expectedCiHash);
        assertThat(saved.getMemberDi()).isEqualTo("DI-001");
        assertThat(saved.getMemberDiHash()).isEqualTo(expectedDiHash);

        // 기본 상태 / 동의값
        assertThat(saved.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(saved.getPrivConsentYn()).isEqualTo("Y");
        assertThat(saved.getMsgConsentYn()).isEqualTo("Y");
    }

    @Test
    @DisplayName("동일 CI로 회원 생성 시 USER_ALREADY_EXISTS 에러가 발생한다")
    void createMember_duplicateCi_throwsException() {
        // given
        MemberDto.CreateReq first = MemberDto.CreateReq.builder()
                .memberNm("첫번째")
                .memberPhone("01011112222")
                .memberCi("CI-DUP")
                .memberDi("DI-1")
                .build();
        memberService.create(first, creatorId);

        MemberDto.CreateReq dup = MemberDto.CreateReq.builder()
                .memberNm("두번째")
                .memberPhone("01022223333")
                .memberCi("CI-DUP")   // 같은 CI
                .memberDi("DI-2")
                .build();

        // when & then
        assertThatThrownBy(() -> memberService.create(dup, creatorId))
                .isInstanceOf(RemittanceException.class)
                .satisfies(ex -> {
                    RemittanceException re = (RemittanceException) ex;
                    assertThat(re.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_EXISTS);
                });
    }

    @Test
    @DisplayName("회원 정보 수정 시 이름/전화번호/동의값이 정상적으로 변경된다")
    void updateMember_success() {
        // given: 먼저 회원 하나 생성
        MemberDto.CreateReq createReq = MemberDto.CreateReq.builder()
                .memberNm("원래이름")
                .memberPhone("010-0000-0000")
                .memberCi("CI-UPD-1")
                .memberDi("DI-UPD-1")
                .privConsentYn("Y")
                .msgConsentYn("Y")
                .build();
        Long memberSeq = memberService.create(createReq, creatorId).getMemberSeq();

        MemberDto.UpdateReq updateReq = MemberDto.UpdateReq.builder()
                .memberSeq(memberSeq)
                .memberNm("변경이름")
                .memberPhone("010-9999-8888")
                .privConsentYn("N")
                .msgConsentYn("N")
                // 상태도 변경 테스트 (예: DELETED 로 변경)
                .memberStatus(MemberStatus.DELETED)
                .build();

        // when
        MemberDto.IdResponse idRes = memberService.update(updateReq, "admin");

        // then
        Member updated = memberRepository.findById(idRes.getMemberSeq())
                .orElseThrow();

        String normalizedPhone = normalizePhone("010-9999-8888");

        assertThat(updated.getMemberNm()).isEqualTo("변경이름");
        assertThat(updated.getMemberPhone()).isEqualTo(normalizedPhone);
        assertThat(updated.getMemberPhoneHash()).isEqualTo(cryptoUtil.hmacHash(normalizedPhone));
        assertThat(updated.getPrivConsentYn()).isEqualTo("N");
        assertThat(updated.getMsgConsentYn()).isEqualTo("N");
        assertThat(updated.getMemberStatus()).isEqualTo(MemberStatus.DELETED);
        assertThat(updated.getModId()).isEqualTo("admin");
    }

    @Test
    @DisplayName("소프트 삭제 시 상태가 DELETED 로 변경된다")
    void deleteSoft_success() {
        // given
        MemberDto.CreateReq createReq = MemberDto.CreateReq.builder()
                .memberNm("삭제대상")
                .memberPhone("010-3333-4444")
                .memberCi("CI-DEL-1")
                .memberDi("DI-DEL-1")
                .build();
        Long memberSeq = memberService.create(createReq, creatorId).getMemberSeq();

        // when
        MemberDto.IdResponse idRes = memberService.deleteSoft(memberSeq, "admin");

        // then
        Member deleted = memberRepository.findById(idRes.getMemberSeq())
                .orElseThrow();
        assertThat(deleted.getMemberStatus()).isEqualTo(MemberStatus.DELETED);
        assertThat(deleted.getModId()).isEqualTo("admin");
    }

    @Test
    @DisplayName("이미 소프트 삭제된 회원을 다시 삭제하면 USER_ALREADY_DELETED 에러가 발생한다")
    void deleteSoft_alreadyDeleted_throwsException() {
        // given
        MemberDto.CreateReq createReq = MemberDto.CreateReq.builder()
                .memberNm("두번삭제")
                .memberPhone("010-5555-6666")
                .memberCi("CI-DEL-2")
                .memberDi("DI-DEL-2")
                .build();
        Long memberSeq = memberService.create(createReq, creatorId).getMemberSeq();
        // 1차 삭제
        memberService.deleteSoft(memberSeq, "admin");

        // when & then
        assertThatThrownBy(() -> memberService.deleteSoft(memberSeq, "admin2"))
                .isInstanceOf(RemittanceException.class)
                .satisfies(ex -> {
                    RemittanceException re = (RemittanceException) ex;
                    assertThat(re.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_DELETED);
                });
    }

    @Test
    @DisplayName("getById 에서 masked 플래그에 따라 마스킹 여부가 다르게 반환된다")
    void getById_maskedFlag_works() {
        // given
        MemberDto.CreateReq createReq = MemberDto.CreateReq.builder()
                .memberNm("마스킹테스트")
                .memberPhone("010-7777-8888")
                .memberCi("CI-MASK-1")
                .memberDi("DI-MASK-1")
                .build();
        Long memberSeq = memberService.create(createReq, creatorId).getMemberSeq();

        String normalizedPhone = normalizePhone("010-7777-8888");

        // when
        MemberDto.Res unmasked = memberService.getById(memberSeq, false);
        MemberDto.Res masked   = memberService.getById(memberSeq, true);

        // then
        // 비마스킹: 원본 번호와 같아야 함
        assertThat(unmasked.getMemberPhone()).isEqualTo(normalizedPhone);
        // 마스킹: 원본 번호와 달라야 함 (정확한 포맷은 MaskingUtils 구현에 위임)
        assertThat(masked.getMemberPhone()).isNotEqualTo(normalizedPhone);
    }

    @Test
    @DisplayName("회원 검색(search) 시 조건에 맞는 회원이 페이지 형태로 조회된다")
    void search_returnsPageResponse() {
        // given: 검색용 회원 2명 생성
        MemberDto.CreateReq req1 = MemberDto.CreateReq.builder()
                .memberNm("검색홍길동")
                .memberPhone("010-1111-2222")
                .memberCi("CI-SRC-1")
                .memberDi("DI-SRC-1")
                .build();
        MemberDto.CreateReq req2 = MemberDto.CreateReq.builder()
                .memberNm("다른사람")
                .memberPhone("010-3333-4444")
                .memberCi("CI-SRC-2")
                .memberDi("DI-SRC-2")
                .build();
        memberService.create(req1, creatorId);
        memberService.create(req2, creatorId);

        MemberDto.SearchReq searchReq = new MemberDto.SearchReq();
        searchReq.setMemberNm("검색"); // like 검색홍길동
        searchReq.setPage(0);
        searchReq.setSize(10);

        // when
        PageDto.PageResponse<MemberDto.Res> pageRes = memberService.search(searchReq, true);

        // then
        assertThat(pageRes).isNotNull();
        assertThat(pageRes.getItems()).isNotEmpty();

        // "검색홍길동" 이름이 포함된 Member가 있는지 검사
        boolean exists = pageRes.getItems().stream()
                .map(MemberDto.Res::getMemberNm)
                .anyMatch(name -> name != null && name.contains("검색홍길동"));

        assertThat(exists).isTrue();
    }
}

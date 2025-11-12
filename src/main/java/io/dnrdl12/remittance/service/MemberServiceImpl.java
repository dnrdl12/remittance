package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.comm.crypto.CryptoUtil;
import io.dnrdl12.remittance.comm.api.PageDto;
import io.dnrdl12.remittance.comm.utills.PageUtils;
import io.dnrdl12.remittance.comm.api.PagingProperties;
import io.dnrdl12.remittance.entity.Member;
import io.dnrdl12.remittance.dto.MemberDto;
import io.dnrdl12.remittance.repository.MemberRepository;
import io.dnrdl12.remittance.repository.MemberSpecs;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : MemberServiceImpl
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PagingProperties pagingProperties;
    private final CryptoUtil cryptoUtil; // encrypt/decrypt는 Converter, 여기선 해시만

    @Override
    @Transactional
    public MemberDto.IdResponse create(MemberDto.CreateReq req, String actorId) {
        // 입력 표준화
        String phonePlain = normalizePhone(req.getMemberPhone());
        String ciPlain    = req.getMemberCi();
        String diPlain    = req.getMemberDi();

        // 검색/중복용 해시 (HMAC-SHA256 Base64)
        String phoneHash = cryptoUtil.hmacHash(phonePlain);
        String ciHash    = cryptoUtil.hmacHash(ciPlain);
        String diHash    = cryptoUtil.hmacHash(diPlain);

        // 중복 정책(예: CI 유니크)
        if (memberRepository.existsByMemberCiHash(ciHash)) {
            throw new IllegalArgumentException("이미 등록된 사용자 입니다.");
        }

        Member e = Member.builder()
                .memberNm(req.getMemberNm())
                .memberPhone(phonePlain)
                .memberPhoneHash(phoneHash)
                .memberCi(ciPlain)
                .memberCiHash(ciHash)
                .memberDi(diPlain)
                .memberDiHash(diHash)
                .memberStatus(1)
                .privConsentYn(req.getPrivConsentYn() == null ? "Y" : req.getPrivConsentYn())
                .msgConsentYn(req.getMsgConsentYn() == null ? "Y" : req.getMsgConsentYn())
                .build();

        e.setRegId(actorId);
        e.setModId(actorId);

        Member saved = memberRepository.save(e);
        return MemberDto.IdResponse.of(saved.getMemberSeq());
    }

    @Override
    @Transactional
    public MemberDto.IdResponse update(MemberDto.UpdateReq req, String actorId) {
        Member e = memberRepository.findById(req.getMemberSeq())
                .orElseThrow(() -> new EntityNotFoundException("사용자이 존재하지 않습니다."));

        if (req.getMemberNm() != null) e.setMemberNm(req.getMemberNm());

        if (req.getMemberPhone() != null) {
            String phonePlain = normalizePhone(req.getMemberPhone());
            e.setMemberPhone(phonePlain); // 암호화는 Converter가 처리
            e.setMemberPhoneHash(cryptoUtil.hmacHash(phonePlain));
        }
        if (req.getMemberCi() != null) {
            e.setMemberCi(req.getMemberCi());
            e.setMemberCiHash(cryptoUtil.hmacHash(req.getMemberCi()));
        }
        if (req.getMemberDi() != null) {
            e.setMemberDi(req.getMemberDi());
            e.setMemberDiHash(cryptoUtil.hmacHash(req.getMemberDi()));
        }
        if (req.getMemberStatus() != null) e.setMemberStatus(req.getMemberStatus());
        if (req.getPrivConsentYn() != null) e.setPrivConsentYn(req.getPrivConsentYn());
        if (req.getMsgConsentYn() != null) e.setMsgConsentYn(req.getMsgConsentYn());

        e.setModId(actorId);
        return MemberDto.IdResponse.of(e.getMemberSeq());
    }

    @Override
    @Transactional
    public MemberDto.IdResponse deleteSoft(Long memberSeq, String actorId) {
        Member e = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("사용자이 존재하지 않습니다."));
        e.setMemberStatus(2); // 논리삭제
        e.setModId(actorId);
        return MemberDto.IdResponse.of(e.getMemberSeq());
    }

    @Override
    public MemberDto.Res getById(Long memberSeq, boolean masked) {
        Member e = memberRepository.findById(memberSeq)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        return MemberDto.Res.from(e, masked);
    }

    @Override
    public PageDto.PageResponse<MemberDto.Res> search(MemberDto.SearchReq req, boolean masked) {
        // 평문 입력 → 해시 변환 후 해시 컬럼으로 검색
        String phoneHash = req.getMemberPhone() == null ? null : cryptoUtil.hmacHash(normalizePhone(req.getMemberPhone()));
        String ciHash    = req.getMemberCi()    == null ? null : cryptoUtil.hmacHash(req.getMemberCi());
        String diHash    = req.getMemberDi()    == null ? null : cryptoUtil.hmacHash(req.getMemberDi());

        Specification<Member> spec = Specification
                .where(MemberSpecs.memberNmContains(req.getMemberNm()))
                .and(MemberSpecs.statusEq(req.getMemberStatus()))
                .and(MemberSpecs.privConsentEq(req.getPrivConsentYn()))
                .and(MemberSpecs.msgConsentEq(req.getMsgConsentYn()))
                .and(MemberSpecs.phoneHashEq(phoneHash))
                .and(MemberSpecs.ciHashEq(ciHash))
                .and(MemberSpecs.diHashEq(diHash));
        // 등록일 범위 필요시: .and(MemberSpecs.regDateBetween(req.getRegFrom(), req.getRegTo()));

        var pageable = PageUtils.toPageable(
                req, pagingProperties.defaultSize(), pagingProperties.maxSize()
        );

        Page<Member> page = memberRepository.findAll(spec, pageable);
        Page<MemberDto.Res> mapped = page.map(m -> MemberDto.Res.from(m, masked));
        return PageUtils.toPageResponse(mapped);
    }

    /** 전화번호 표준화(선택): 숫자만 남기기 */
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9]", "");
    }
}
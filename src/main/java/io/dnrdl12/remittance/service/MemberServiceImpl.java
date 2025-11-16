package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.comm.crypto.CryptoUtil;
import io.dnrdl12.remittance.comm.api.PageDto;
import io.dnrdl12.remittance.comm.enums.ErrorCode;
import io.dnrdl12.remittance.comm.enums.MemberStatus;
import io.dnrdl12.remittance.comm.exception.RemittanceExceptionFactory;
import io.dnrdl12.remittance.comm.utills.PageUtils;
import io.dnrdl12.remittance.comm.api.PagingProperties;
import io.dnrdl12.remittance.entity.Member;
import io.dnrdl12.remittance.dto.MemberDto;
import io.dnrdl12.remittance.repository.MemberRepository;
import io.dnrdl12.remittance.spec.MemberSpecs;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static io.dnrdl12.remittance.comm.utills.StringUtils.normalizePhone;

/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : MemberServiceImpl
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI            최초 생성
 * 2025-11-16        JW.CHOI            에러 공통화
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PagingProperties pagingProperties;
    private final CryptoUtil cryptoUtil;

    @Override
    @Transactional
    public MemberDto.IdResponse create(MemberDto.CreateReq req, String userId) {
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
            throw RemittanceExceptionFactory.of(ErrorCode.USER_ALREADY_EXISTS);
        }

        Member e = Member.builder()
                .memberNm(req.getMemberNm())
                .memberPhone(phonePlain)
                .memberPhoneHash(phoneHash)
                .memberCi(ciPlain)
                .memberCiHash(ciHash)
                .memberDi(diPlain)
                .memberDiHash(diHash)
                .memberStatus(MemberStatus.ACTIVE)
                .privConsentYn(req.getPrivConsentYn() == null ? "Y" : req.getPrivConsentYn())
                .msgConsentYn(req.getMsgConsentYn() == null ? "Y" : req.getMsgConsentYn())
                .build();

        e.setRegId(userId);
        e.setModId(userId);

        Member saved = memberRepository.save(e);
        return MemberDto.IdResponse.of(saved.getMemberSeq());
    }

    @Override
    @Transactional
    public MemberDto.IdResponse update(MemberDto.UpdateReq req, String userId) {
        Member e = memberRepository.findById(req.getMemberSeq())
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.USER_NOT_FOUND));
        /*
        * 삭제된 사용자 복구 시 사용필요 or 추후 별도의복구기능 생성 시 추가
        * if(2 == e.getMemberStatus())  throw  new EntityNotFoundException("삭제된 사용자 입니다.");
        * */

        if (req.getMemberNm() != null) e.setMemberNm(req.getMemberNm());

        if (req.getMemberPhone() != null) {
            String phonePlain = normalizePhone(req.getMemberPhone());
            e.setMemberPhone(phonePlain); // 암호화는 Converter가 처리
            e.setMemberPhoneHash(cryptoUtil.hmacHash(phonePlain));
        }
        if (req.getMemberStatus() != null) e.setMemberStatus(req.getMemberStatus());
        if (req.getPrivConsentYn() != null) e.setPrivConsentYn(req.getPrivConsentYn());
        if (req.getMsgConsentYn() != null) e.setMsgConsentYn(req.getMsgConsentYn());
        e.setModId(userId);
        return MemberDto.IdResponse.of(e.getMemberSeq());
    }

    @Override
    @Transactional
    public MemberDto.IdResponse deleteSoft(Long memberSeq, String userId) {
        Member e = memberRepository.findById(memberSeq)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.USER_NOT_FOUND));

        if(MemberStatus.DELETED.getCode() == e.getMemberStatus().getCode())  throw RemittanceExceptionFactory.of(ErrorCode.USER_ALREADY_DELETED);

        e.setMemberStatus(MemberStatus.DELETED);
        e.setModId(userId);
        return MemberDto.IdResponse.of(e.getMemberSeq());
    }

    @Override
    public MemberDto.Res getById(Long memberSeq, boolean masked) {
        Member e = memberRepository.findById(memberSeq)
                .orElseThrow(() -> RemittanceExceptionFactory.of(ErrorCode.USER_NOT_FOUND));
        //삭제되도 검색은 되어야됨 추구 변경가능
        //if(MemberStatus.DELETED.getCode() == e.getMemberStatus())  throw  new EntityNotFoundException("이미 삭제된 사용자 입니다.");

        return MemberDto.Res.from(e, masked);
    }

    @Override
    public PageDto.PageResponse<MemberDto.Res> search(MemberDto.SearchReq req, boolean masked) {
        String phoneHash = req.getMemberPhone() == null ? null : cryptoUtil.hmacHash(normalizePhone(req.getMemberPhone()));
        String ciHash    = req.getMemberCi()    == null ? null : cryptoUtil.hmacHash(req.getMemberCi());
        String diHash    = req.getMemberDi()    == null ? null : cryptoUtil.hmacHash(req.getMemberDi());

        //해지된사람도 검색
        Specification<Member> spec = Specification
                .where(MemberSpecs.memberNmContains(req.getMemberNm()))
                .and(MemberSpecs.statusEq(req.getMemberStatus() != null ? req.getMemberStatus().getCode() : null))
                .and(MemberSpecs.privConsentEq(req.getPrivConsentYn()))
                .and(MemberSpecs.msgConsentEq(req.getMsgConsentYn()))
                .and(MemberSpecs.phoneHashEq(phoneHash))
                .and(MemberSpecs.ciHashEq(ciHash))
                .and(MemberSpecs.diHashEq(diHash));

        Pageable pageable = PageRequest.of(
                req.getPage() != null ? req.getPage() : 0,
                req.getSize() != null ? Math.min(req.getSize(), pagingProperties.maxSize()) : pagingProperties.defaultSize(),
                Sort.by(Sort.Direction.DESC, "memberSeq")
        );

        Page<Member> page = memberRepository.findAll(spec, pageable);
        Page<MemberDto.Res> mapped = page.map(m -> MemberDto.Res.from(m, masked));
        return PageUtils.toPageResponse(mapped);
    }
}
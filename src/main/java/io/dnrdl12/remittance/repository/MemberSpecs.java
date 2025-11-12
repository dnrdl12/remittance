package io.dnrdl12.remittance.repository;
import io.dnrdl12.remittance.entity.Member;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
/**
 * packageName    : io.dnrdl12.remittance.repository
 * fileName       : MemberSpecs
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    : 검색용
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */

public final class MemberSpecs {

    private MemberSpecs() {}

    public static Specification<Member> memberNmContains(String memberNm) {
        return (root, q, cb) -> memberNm == null || memberNm.isBlank()
                ? null
                : cb.like(root.get("memberNm"), "%" + memberNm + "%");
    }

    public static Specification<Member> statusEq(Integer status) {
        return (root, q, cb) -> status == null ? null : cb.equal(root.get("memberStatus"), status);
    }

    public static Specification<Member> privConsentEq(String yn) {
        return (root, q, cb) -> yn == null ? null : cb.equal(root.get("privConsentYn"), yn);
    }

    public static Specification<Member> msgConsentEq(String yn) {
        return (root, q, cb) -> yn == null ? null : cb.equal(root.get("msgConsentYn"), yn);
    }

    public static Specification<Member> phoneHashEq(String phoneHash) {
        return (root, q, cb) -> phoneHash == null ? null : cb.equal(root.get("memberPhoneHash"), phoneHash);
    }

    public static Specification<Member> ciHashEq(String ciHash) {
        return (root, q, cb) -> ciHash == null ? null : cb.equal(root.get("memberCiHash"), ciHash);
    }

    public static Specification<Member> diHashEq(String diHash) {
        return (root, q, cb) -> diHash == null ? null : cb.equal(root.get("memberDiHash"), diHash);
    }

    public static Specification<Member> regDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, q, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("regDate"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("regDate"), from);
            return cb.lessThanOrEqualTo(root.get("regDate"), to);
        };
    }
}
package io.dnrdl12.remittance.repository;

import io.dnrdl12.remittance.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * packageName    : io.dnrdl12.remittance.repository
 * fileName       : AccountUserRepository
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 해시 컬럼 기반 단건 조회 (중복 체크/식별용)
    Optional<Member> findByMemberDiHash(String memberDiHash);
    Optional<Member> findByMemberCiHash(String memberCiHash);
    Optional<Member> findByMemberPhoneHash(String memberPhoneHash);

    // 상태값 기반 조회(예: 활성 회원만)
    Optional<Member> findByMemberSeqAndMemberStatus(Long memberSeq, Integer memberStatus);

    // 필요한 경우 카운트/존재 여부
    boolean existsByMemberDiHash(String memberDiHash);
    boolean existsByMemberCiHash(String memberCiHash);
    boolean existsByMemberPhoneHash(String memberPhoneHash);

    Page<Member> findAll(Specification<Member> spec, Pageable pageable);
}
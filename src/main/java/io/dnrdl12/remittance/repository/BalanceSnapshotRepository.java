package io.dnrdl12.remittance.repository;

import io.dnrdl12.remittance.entity.BalanceSnapshot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * packageName    : io.dnrdl12.remittance.repository
 * fileName       : BalanceSnapshotRepository
 * author         : JW.CHOI
 * date           : 2025-11-15
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-15        JW.CHOI              최초 생성
 */
public interface BalanceSnapshotRepository extends JpaRepository<BalanceSnapshot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BalanceSnapshot b where b.accountSeq = :accountSeq")
    Optional<BalanceSnapshot> findByAccountSeqForUpdate(@Param("accountSeq") Long accountSeq);

    @Query("select b from BalanceSnapshot b where b.accountSeq = :accountSeq")
    Optional<BalanceSnapshot> findByAccountSeq(@Param("accountSeq") Long accountSeq);

}
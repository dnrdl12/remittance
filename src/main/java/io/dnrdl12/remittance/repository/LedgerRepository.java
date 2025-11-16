package io.dnrdl12.remittance.repository;
import io.dnrdl12.remittance.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * packageName    : io.dnrdl12.remittance.repository
 * fileName       : LedgerRepository
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */

public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    @Query("select coalesce(sum(l.amount), 0) from Ledger l where l.accountSeq = :accountSeq")
    Long sumAmountByAccountSeq(@Param("accountSeq") Long accountSeq);
}
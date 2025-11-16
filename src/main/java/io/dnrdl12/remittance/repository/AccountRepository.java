package io.dnrdl12.remittance.repository;

import io.dnrdl12.remittance.comm.enums.AccountStatus;
import io.dnrdl12.remittance.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * packageName    : io.dnrdl12.remittance.repository
 * fileName       : AccountRepository
 * author         : JW.CHOI
 * date           : 2025-11-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-14        JW.CHOI              최초 생성
 */
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account>  {

    /**
     * 동적 검색용 (간단 조회)
     * - Account + Member 까지만 fetch
     */
    @Override
    @EntityGraph(attributePaths = {"member"})
    Page<Account> findAll(org.springframework.data.jpa.domain.Specification<Account> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "balanceSnapshot", "feePolicy"})
    Optional<Account> findByAccountSeq(Long accountSeq);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountSeq = :accountSeq")
    Optional<Account> findByIdForUpdate(@Param("accountSeq") Long accountSeq);
}
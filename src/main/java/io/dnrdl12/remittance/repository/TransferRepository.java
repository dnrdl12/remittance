package io.dnrdl12.remittance.repository;


import io.dnrdl12.remittance.comm.enums.ClientId;
import io.dnrdl12.remittance.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * packageName    : io.dnrdl12.remittance.repository
 * fileName       : TransferRepository
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByClientIdAndIdempotencyKey(ClientId clientId, String idempotencyKey);
}
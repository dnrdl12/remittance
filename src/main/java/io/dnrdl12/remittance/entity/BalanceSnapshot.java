package io.dnrdl12.remittance.entity;

import io.dnrdl12.remittance.comm.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * packageName    : io.dnrdl12.remittance.entity
 * fileName       : BalanceSnapshot
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    : 계좌 잔액 스냅샷 (조회 성능용 캐시)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI            최초 생성
 */
@Entity
@Table(name = "balance_snapshots")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceSnapshot extends BaseEntity {

    @Id
    @Schema(description = "계좌 ID")
    @Column(name = "account_seq")
    private Long accountSeq;

    @Schema(description = "스냅샷 잔액")
    @Column(name = "balance", nullable = false)
    private Long balance;
}

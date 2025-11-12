package io.dnrdl12.remittance.entity;

import io.dnrdl12.remittance.comm.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * packageName    : io.dnrdl12.remittance.entity
 * fileName       : FeePolicy
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    : 이체 수수료 정책 엔티티
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI            최초 생성
 */
@Entity
@Table(name = "fee_policy")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class FeePolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "수수료 정책 ID")
    @Column(name = "fee_policy_seq")
    private Long feePolicySeq;

    @Schema(description = "정책명")
    @Column(name = "policy_name", nullable = false, length = 50)
    private String policyName;

    @Schema(description = "이체 수수료율 (기본 0.001)")
    @Column(name = "transfer_fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal transferFeeRate = new BigDecimal("0.001");

    @Schema(description = "출금 수수료율 (기본 0.000)")
    @Column(name = "withdraw_fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal withdrawFeeRate = new BigDecimal("0.000");

    @Schema(description = "이벤트 적용 여부")
    @Column(name = "event_flag")
    private Boolean eventFlag = Boolean.FALSE;

    @Schema(description = "이벤트 시작일")
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Schema(description = "이벤트 종료일")
    @Column(name = "end_date")
    private LocalDateTime endDate;
}

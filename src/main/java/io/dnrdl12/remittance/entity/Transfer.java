package io.dnrdl12.remittance.entity;

import io.dnrdl12.remittance.comm.entity.BaseEntity;
import io.dnrdl12.remittance.comm.enums.TransferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * packageName    : io.dnrdl12.remittance.entity
 * fileName       : Transfer
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    : 거래장부 1줄 (이체 전표)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI            최초 생성
 */
@Entity
@Table(
    name = "transfers",
    uniqueConstraints = { @UniqueConstraint(name = "uk_transfers_client_id_idem", columnNames = {"client_id", "idempotency_key"}) },
    indexes = { @Index(name = "ix_transfers_status_requested_date", columnList = "status,requested_date") }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class Transfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "이체 seq")
    @Column(name = "transfer_seq")
    private Long transferSeq;

    @Schema(description = "클라이언트/채널 구분")
    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Schema(description = "요청 중복 방지 키 (Idempotency Key)")
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Schema(description = "출금 계좌 seq (FK)")
    @Column(name = "from_account_seq", nullable = false)
    private Long fromAccountSeq;

    @Schema(description = "입금 계좌 seq (FK)")
    @Column(name = "to_account_seq", nullable = false)
    private Long toAccountSeq;

    @Schema(description = "이체 금액(원)")
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Schema(description = "수수료(출금측)")
    @Column(name = "fee", nullable = false)
    private Long fee = 0L;

    @Schema(description = "통화 단위 (기본 KRW)")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";

    @Schema(description = "이체 상태 (PENDING, POSTED, FAILED, CANCELLED)")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TransferStatus status = TransferStatus.PENDING;

    @Schema(description = "실패 사유 코드")
    @Column(name = "fail_code", length = 64)
    private String failCode;

    @Schema(description = "거래 메모")
    @Column(name = "memo", length = 255)
    private String memo;

    @Schema(description = "거래일시 (DB default CURRENT_TIMESTAMP)")
    @Column(
            name = "requested_date",
            insertable = false,
            updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime requestedDate;

    @Schema(description = "거래 확정일시 (POSTED 시점)")
    @Column(name = "posted_date")
    private LocalDateTime postedDate;
}

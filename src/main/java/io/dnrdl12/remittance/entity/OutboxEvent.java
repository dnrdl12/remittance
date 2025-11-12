package io.dnrdl12.remittance.entity;

import io.dnrdl12.remittance.comm.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * packageName    : io.dnrdl12.remittance.entity
 * fileName       : OutboxEvent
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    : 외부 시스템 이벤트 발행을 위한 Outbox 테이블
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI            최초 생성
 */
@Entity
@Table(
    name = "outbox_events",
    indexes = { @Index(name = "ix_outbox_pub_create", columnList = "published_date,created_date") }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class OutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "아웃박스 이벤트 ID")
    @Column(name = "outbox_seq")
    private Long outboxSeq;

    @Schema(description = "Aggregate 타입 (예: 'transfer')")
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Schema(description = "Aggregate ID (예: transfers.transfer_seq)")
    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Schema(description = "이벤트 타입 (예: TRANSFER_POSTED)")
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Schema(description = "이벤트 본문(JSON)")
    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    private String payload;

    @Schema(description = "생성일시 (DB default CURRENT_TIMESTAMP)")
    @Column(
            name = "created_date",
            insertable = false,
            updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime createdDate;

    @Schema(description = "발행 완료 일시")
    @Column(name = "published_date")
    private LocalDateTime publishedDate;
}

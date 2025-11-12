package io.dnrdl12.remittance.entity;

import io.dnrdl12.remittance.comm.entity.BaseEntity;
import io.dnrdl12.remittance.comm.enums.EntryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * packageName    : io.dnrdl12.remittance.entity
 * fileName       : LedgerEntry
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    : 거래 원장 분개 (한 전표에 대해 최소 2줄, 합계=0 원칙)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI            최초 생성
 */
@Entity
@Table(
    name = "ledger_entries",
    indexes = {
        @Index(name = "ix_ledger_account_entry_time", columnList = "account_seq,entry_time"),
        @Index(name = "ix_ledger_transfer_seq", columnList = "transfer_seq")
    }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class Ledger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "원장 분개 ID")
    @Column(name = "ledger_seq")
    private Long ledgerSeq;

    @Schema(description = "거래장부 seq (FK)")
    @Column(name = "transfer_seq", nullable = false)
    private Long transferSeq;

    @Schema(description = "대상 계좌 seq (FK)")
    @Column(name = "account_seq", nullable = false)
    private Long accountSeq;

    @Schema(description = "금액 (출금 음수 / 입금 양수)")
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Schema(description = "분개 유형 (DEBIT/CREDIT)")
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @Schema(description = "통화 (기본 KRW)")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";

    @Schema(description = "분개 시각 (DB default CURRENT_TIMESTAMP)")
    @Column(
            name = "entry_time",
            insertable = false,
            updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime entryTime;
}

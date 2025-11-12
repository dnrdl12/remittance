package io.dnrdl12.remittance.entity;

import io.dnrdl12.remittance.comm.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * packageName    : io.dnrdl12.remittance.entity
 * fileName       : Account
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    : 계좌 엔티티 (BaseEntity 상속)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI            최초 생성
 */
@Entity
@Table(name = "account",
        indexes = {
                @Index(name = "ix_account_member", columnList = "member_seq")
        })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "계좌 고유 식별자")
    @Column(name = "account_seq")
    private Long accountSeq;

    @Schema(description = "계좌번호")
    @Column(name = "account_number", nullable = false, unique = true, length = 30)
    private String accountNumber;

    @Schema(description = "회원 고유 식별자 (FK)")
    @Column(name = "member_seq", nullable = false)
    private Long memberSeq;

    @Schema(description = "계좌 상태: 1 정상, 2 정지, 3 해지")
    @Column(name = "account_status", nullable = false)
    private Integer accountStatus = 1;

    @Schema(description = "계좌 별칭")
    @Column(name = "nickname", length = 50)
    private String nickname;

    @Schema(description = "인터넷뱅킹 가능 여부")
    @Column(name = "online_enabled")
    private Boolean onlineEnabled = Boolean.TRUE;

    @Schema(description = "계좌 종류: 1 일반, 2 월급통장, 3 한도통장")
    @Column(name = "account_type")
    private Integer accountType = 1;

    @Schema(description = "은행 코드")
    @Column(name = "bank_code", length = 10)
    private String bankCode;

    @Schema(description = "지점 코드")
    @Column(name = "branch_code", length = 10)
    private String branchCode;

    @Schema(description = "계좌 개설일시")
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Schema(description = "계좌 해지일시")
    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Schema(description = "계좌 순번")
    @Column(name = "account_sort")
    private Integer accountSort;

    @Schema(description = "적용 수수료 정책 FK")
    @Column(name = "fee_policy_seq")
    private Long feePolicySeq;

    @Schema(description = "1일 이체 한도")
    @Column(name = "daily_transfer_limit", nullable = false)
    private Long dailyTransferLimit = 3_000_000L;

    @Schema(description = "1일 출금 한도")
    @Column(name = "daily_withdraw_limit", nullable = false)
    private Long dailyWithdrawLimit = 1_000_000L;
}

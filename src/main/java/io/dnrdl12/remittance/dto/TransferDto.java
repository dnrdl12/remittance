package io.dnrdl12.remittance.dto;

import io.dnrdl12.remittance.comm.enums.ClientId;
import io.dnrdl12.remittance.comm.enums.FailCode;
import io.dnrdl12.remittance.comm.enums.TransferStatus;
import io.dnrdl12.remittance.entity.Transfer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
/**
 * packageName    : io.dnrdl12.remittance.dto
 * fileName       : TransferDto
 * author         : JW.CHOI
 * date           : 2025-11-15
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-15        JW.CHOI              최초 생성
 */
public class TransferDto {

    // ========= 공통 요청 필드 =========
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class BaseReq {

        @Schema(description = "요청 채널 ID", example = "WEB")
        @NotNull
        private ClientId clientId;

        @Schema(description = "멱등성 키 (같은 요청은 같은 키 사용)", example = "20251116-DEP-0001")
        @NotBlank
        private String idempotencyKey;
    }

    // ========= 입금 요청 =========
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Schema(name = "TransferDto.DepositReq", description = "입금 요청 DTO")
    public static class DepositReq extends BaseReq {

        @Schema(description = "입금 대상 계좌", example = "999-0000-000001")
        @NotNull
        private String accountNumber;

        @Schema(description = "입금 금액", example = "100000")
        @NotNull
        @Positive
        private Long amount;
    }

    // ========= 출금 요청 =========
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Schema(name = "TransferDto.WithdrawReq", description = "출금 요청 DTO")
    public static class WithdrawReq extends BaseReq {

        @Schema(description = "출금 대상 계좌", example = "999-0000-000001")
        @NotNull
        private String accountNumber;

        @Schema(description = "출금 금액", example = "50000")
        @NotNull
        @Positive
        private Long amount;
    }

    // ========= 계좌간 이체 요청 =========
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Schema(name = "TransferDto.TransferReq", description = "계좌간 이체 요청 DTO")
    public static class TransferReq extends BaseReq {

        @Schema(description = "출금 계좌", example = "999-0000-000001")
        @NotNull
        private String fromAccountNumber;

        @Schema(description = "입금 계좌", example = "999-0000-000002")
        @NotNull
        private String toAccountNumber;

        @Schema(description = "이체 금액", example = "20000")
        @NotNull
        @Positive
        private Long amount;
    }

    // ========= 공통 응답 =========
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "TransferDto.TransferRes", description = "거래 응답 DTO")
    public static class TransferRes {

        @Schema(description = "거래 SEQ", example = "1")
        private Long transferSeq;

        @Schema(description = "출금 계좌 SEQ", example = "1001")
        private Long fromAccountSeq;

        @Schema(description = "입금 계좌 SEQ", example = "1002")
        private Long toAccountSeq;

        @Schema(description = "거래 금액 (상대 계좌에게 전달된 금액)", example = "20000")
        private Long amount;

        @Schema(description = "수수료 금액", example = "100")
        private Long fee;

        @Schema(description = "통화", example = "KRW")
        private String currency;

        @Schema(description = "거래 상태", example = "POSTED")
        private TransferStatus status;

        @Schema(description = "실패 코드 (실패 시)", example = "INSUFFICIENT_BALANCE")
        private FailCode failCode;

        @Schema(description = "거래 요청 일시", example = "2025-11-16T10:15:30")
        private LocalDateTime requestedDate;

        @Schema(description = "거래 확정 일시 (POSTED 시점)", example = "2025-11-16T10:15:31")
        private LocalDateTime postedDate;

        public static TransferRes from(Transfer t) {
            if (t == null) return null;
            return TransferRes.builder()
                    .transferSeq(t.getTransferSeq())
                    .fromAccountSeq(t.getFromAccountSeq())
                    .toAccountSeq(t.getToAccountSeq())
                    .amount(t.getAmount())
                    .fee(t.getFee())
                    .currency(t.getCurrency())
                    .status(t.getStatus())
                    .failCode(t.getFailCode())
                    .requestedDate(t.getRequestedDate())
                    .postedDate(t.getPostedDate())
                    .build();
        }
    }
}
package io.dnrdl12.remittance.dto;

import io.dnrdl12.remittance.comm.api.PageDto;
import io.dnrdl12.remittance.comm.config.AppAccountProperties;
import io.dnrdl12.remittance.comm.enums.AccountStatus;
import io.dnrdl12.remittance.comm.enums.AccountType;
import io.dnrdl12.remittance.comm.enums.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * packageName    : io.dnrdl12.remittance.dto
 * fileName       : AccountDto
 * author         : JW.CHOI
 * date           : 2025-11-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-14        JW.CHOI              최초 생성
 */
public class AccountDto {

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.experimental.SuperBuilder
    @Schema(name = "AccountDto.SearchReq", description = "계좌 검색 요청 DTO")
    public static class SearchReq extends PageDto.PageReq {

        @Schema(description = "회원명", example = "최정욱")
        private String memberNm;

        @Schema(description = "회원 seq", example = "1")
        private Long memberSeq;

        @Schema(description = "계좌 번호", example = "1234567890123")
        private String accountNumber;

        @Schema(description = "계좌 고유 식별자", example = "1")
        private Long accountSeq;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AccountDto.SearchSimpleRes", description = "계좌 검색 응답 - 간단")
    public static class SearchSimpleRes {

        // Account
        @Schema(description = "계좌 고유 식별자", example = "1")
        private Long accountSeq;

        @Schema(description = "계좌 번호", example = "1234567890123")
        private String accountNumber;

        @Schema(description = "계좌 별칭", example = "월급통장")
        private String nickname;

        @Schema(description = "계좌 상태: 1 정상, 2 정지, 3 해지", example = "1")
        private AccountStatus accountStatus;

        @Schema(description = "계좌 종류: 1 일반, 2 월급통장, 3 한도통장", example = "1")
        private AccountType accountType;

        @Schema(description = "은행 코드", example = "999")
        private String bankCode;

        @Schema(description = "지점 코드", example = "001")
        private String branchCode;

        // Member
        @Schema(description = "회원명", example = "최정욱")
        private String memberNm;

        @Schema(description = "회원 전화번호", example = "01012345678")
        private String memberPhone;

        @Schema(description = "회원 상태", example = "1")
        private MemberStatus memberStatus;

        @Schema(description = "개인정보 동의 여부(Y/N)", example = "Y")
        private String privConsentYn;

        @Schema(description = "알림 메시지 수신 동의 여부(Y/N)", example = "Y")
        private String msgConsentYn;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AccountDto.SearchDetailRes", description = "계좌 검색 응답 - 상세")
    public static class SearchDetailRes {

        // Account (필요시 전부 넣어도 됨)
        @Schema(description = "계좌 고유 식별자", example = "1")
        private Long accountSeq;

        @Schema(description = "계좌 번호", example = "1234567890123")
        private String accountNumber;

        @Schema(description = "계좌 별칭", example = "월급통장")
        private String nickname;

        @Schema(description = "계좌 상태: 1 정상, 2 정지, 3 해지", example = "1")
        private AccountStatus accountStatus;

        @Schema(description = "계좌 종류: 1 일반, 2 월급통장, 3 한도통장", example = "1")
        private AccountType accountType;

        @Schema(description = "은행 코드", example = "999")
        private String bankCode;

        @Schema(description = "지점 코드", example = "001")
        private String branchCode;

        @Schema(description = "개설일", example = "2025-08-11")
        private LocalDateTime createdDate;

        // Member
        @Schema(description = "회원명", example = "최정욱")
        private String memberNm;

        @Schema(description = "회원 전화번호", example = "01012345678")
        private String memberPhone;

        @Schema(description = "회원 상태", example = "1")
        private MemberStatus memberStatus;

        @Schema(description = "개인정보 동의 여부(Y/N)", example = "Y")
        private String privConsentYn;

        @Schema(description = "알림 메시지 수신 동의 여부(Y/N)", example = "Y")
        private String msgConsentYn;

        // BalanceSnapshot
        @Schema(description = "현재 잔액", example = "500000")
        private Long balance;

        // FeePolicy
        @Schema(description = "수수료 정책명", example = "기본수수료정책")
        private String policyName;

        @Schema(description = "이체 수수료율", example = "0.001")
        private BigDecimal transferFeeRate;

        @Schema(description = "출금 수수료율", example = "0.001")
        private BigDecimal withdrawFeeRate;
    }


    // ── 계좌 생성 요청 ─────────────────────────────────────────────
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AccountDto.CreateReq", description = "계좌 생성 요청 DTO")
    public static class CreateReq {

        @NotNull
        @Schema(description = "회원 고유 식별자(FK)", example = "1")
        private Long memberSeq;

        @NotNull
        @Schema(description = "계좌 별칭(사용자 지정 이름)", example = "월급통장")
        private String nickname;

        @NotNull
        @Schema(description = "계좌 종류: 1 일반, 2 월급통장, 3 한도통장", example = "1")
        @Min(value = 1, message = "계좌 종류: 값은 1, 2, 3만 허용됩니다.")
        @Max(value = 3, message = "계좌 종류: 값은 1, 2, 3만 허용됩니다.")
        private AccountType accountType;

        @Schema(description = "은행 코드(기본값 999)", example = "999")
        private String bankCode;

        @Schema(description = "지점 코드(기본값 001)", example = "001")
        private String branchCode;

        @Schema(description = "수수료 정책 식별자(기본값 1)", example = "1", defaultValue = "1")
        private Long feePolicySeq;

        @Schema(description = "1일 이체 한도(기본값 3,000,000원)", example = "3000000")
        private Long dailyTransferLimit;

        @Schema(description = "1일 출금 한도(기본값 1,000,000원)", example = "1000000")
        private Long dailyWithdrawLimit;

    }

    // ── 계좌 수정 요청 ─────────────────────────────────────────────
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "AccountDto.PatchReq", description = "계좌 수정 요청")
    public static class PatchReq {

        @Schema(description = "계좌 별칭", example = "생활비통장")
        private String nickname;

        @Schema(description = "계좌 상태: 1 정상, 2 정지, 3 해지", example = "1")
        @Min(value = 1, message = "계좌 상태 값은 1, 2, 3만 허용됩니다.")
        @Max(value = 3, message = "계좌 상태 값은 1, 2, 3만 허용됩니다.")
        private Integer accountStatusCode;

        public AccountStatus toAccountStatusOrNull() {
            return accountStatusCode == null ? null : AccountStatus.fromCode(accountStatusCode);
        }
    }

    // ── 계좌 응답 ─────────────────────────────────────────────────
    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(name = "AccountDto.Res", description = "계좌 응답")
    public static class Res {

        @Schema(description = "계좌 seq")
        private final Long accountSeq;

        @Schema(description = "계좌 번호")
        private final String accountNumber;

        @Schema(description = "회원 seq")
        private final Long memberSeq;

        @Schema(description = "계좌 별칭")
        private final String nickname;

        @Schema(description = "잔액")
        private final Long balance;

        @Schema(description = "계좌 상태: 1 정상, 2 해지")
        private final AccountStatus accountStatus;

        @Schema(description = "생성일시")
        private final LocalDateTime createdDate;
    }

    // 단건 식별자 응답
    @Getter
    @AllArgsConstructor(staticName = "of")
    @Schema(name = "AccountDto.IdResponse", description = "계좌 식별자 응답 DTO")
    public static class IdResponse {
        @Schema(description = "계좌 SEQ", example = "1")
        private final Long accountSeq;
    }

}
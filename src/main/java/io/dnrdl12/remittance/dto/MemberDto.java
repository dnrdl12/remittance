package io.dnrdl12.remittance.dto;

import io.dnrdl12.remittance.comm.api.PageDto;
import io.dnrdl12.remittance.comm.utills.MaskingUtils;
import io.dnrdl12.remittance.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * packageName    : io.dnrdl12.remittance.dto
 * fileName       : AccountUserDto
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */

@Schema(name = "MemberDto", description = "회원 CRUD용 DTO 묶음 (마스킹 지원)")
public class MemberDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateReq {
        @NotBlank private String memberNm;
        @NotBlank private String memberPhone;
        @NotBlank private String memberCi;
        @NotBlank private String memberDi;
        @Pattern(regexp = "Y|N") private String privConsentYn = "Y";
        @Pattern(regexp = "Y|N") private String msgConsentYn = "Y";
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateReq {
        @NotNull private Long memberSeq;
        private String memberNm;
        private String memberPhone;
        private String memberCi;
        private String memberDi;
        private Integer memberStatus;
        @Pattern(regexp = "Y|N") private String privConsentYn;
        @Pattern(regexp = "Y|N") private String msgConsentYn;
    }

    // 검색 + 페이징 요청
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @lombok.experimental.SuperBuilder
    public static class SearchReq extends PageDto.PageReq {
        private String memberNm;
        private String memberPhone; // 평문 입력 → 서비스에서 hash 변환 검색
        private String memberCi;    // 평문 입력 → 서비스에서 hash 변환 검색
        private String memberDi;    // 평문 입력 → 서비스에서 hash 변환 검색
        private Integer memberStatus;
        @Pattern(regexp = "Y|N") private String privConsentYn;
        @Pattern(regexp = "Y|N") private String msgConsentYn;
    }


    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Res {
        private Long memberSeq;
        private String memberNm;
        private String memberPhone;
        private String memberCi;
        private String memberDi;
        private Integer memberStatus;
        private String privConsentYn;
        private String msgConsentYn;
        private String regId;
        private LocalDateTime regDate;
        private String modId;
        private LocalDateTime modDate;

        /** 엔티티 → DTO (마스킹 미적용) */
        public static Res from(io.dnrdl12.remittance.entity.Member e) {
            if (e == null) return null;
            return base(e, false);
        }

        /** 엔티티 → DTO (마스킹 적용 여부 선택) */
        public static Res from(io.dnrdl12.remittance.entity.Member e, boolean masked) {
            if (e == null) return null;
            return base(e, masked);
        }

        private static Res base(io.dnrdl12.remittance.entity.Member e, boolean masked) {
            String phone = e.getMemberPhone();
            String ci = e.getMemberCi();
            String di = e.getMemberDi();

            if (masked) {
                phone = MaskingUtils.maskPhone(phone);
                ci    = MaskingUtils.maskCi(ci);
                di    = MaskingUtils.maskDi(di);
            }

            return Res.builder()
                    .memberSeq(e.getMemberSeq())
                    .memberNm(e.getMemberNm())
                    .memberPhone(phone)
                    .memberCi(ci)
                    .memberDi(di)
                    .memberStatus(e.getMemberStatus())
                    .privConsentYn(e.getPrivConsentYn())
                    .msgConsentYn(e.getMsgConsentYn())
                    .regId(e.getRegId())
                    .regDate(e.getRegDate())
                    .modId(e.getModId())
                    .modDate(e.getModDate())
                    .build();
        }
    }

    // 단건 식별자 응답
    @Getter @AllArgsConstructor(staticName = "of")
    public static class IdResponse {
        private final Long memberSeq;
    }
}
package io.dnrdl12.remittance.dto;

import io.dnrdl12.remittance.comm.api.PageDto;
import io.dnrdl12.remittance.comm.enums.MemberStatus;
import io.dnrdl12.remittance.comm.utills.MaskingUtils;
import io.dnrdl12.remittance.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.Comment;

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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "MemberDto.CreateReq", description = "회원 생성 요청")
    public static class CreateReq {

        @Schema(description = "회원 이름", example = "홍길동")
        @NotBlank
        private String memberNm;

        @Schema(description = "회원 전화번호(평문, 서버에서 암호화/해시 처리)", example = "01012345678")
        @NotBlank
        private String memberPhone;

        @Schema(description = "CI(평문, 서버에서 암호화/해시 처리)")
        @NotBlank
        private String memberCi;

        @Schema(description = "DI(평문, 서버에서 암호화/해시 처리)")
        @NotBlank
        private String memberDi;

        @Schema(description = "개인정보 수집/이용 동의 여부 (Y/N)", example = "Y", defaultValue = "Y")
        @Pattern(regexp = "Y|N")
        private String privConsentYn = "Y";

        @Schema(description = "문자 수신 동의 여부 (Y/N)", example = "Y", defaultValue = "Y")
        @Pattern(regexp = "Y|N")
        private String msgConsentYn = "Y";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "MemberDto.UpdateReq", description = "회원 수정 요청")
    public static class UpdateReq {

        @Schema(description = "회원 SEQ", example = "1")
        @NotNull
        private Long memberSeq;

        @Schema(description = "회원 이름", example = "홍길동")
        private String memberNm;

        @Schema(description = "회원 전화번호(평문, 서버에서 암호화/해시 처리)", example = "01098765432")
        private String memberPhone;

        @Schema(description = "회원 상태 (1: 정상, 2: 삭제 등)", example = "1")
        private MemberStatus memberStatus;

        @Schema(description = "개인정보 수집/이용 동의 여부 (Y/N)", example = "Y")
        @Pattern(regexp = "Y|N")
        private String privConsentYn;

        @Schema(description = "문자 수신 동의 여부 (Y/N)", example = "N")
        @Pattern(regexp = "Y|N")
        private String msgConsentYn;
    }

    // 검색 + 페이징 요청
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.experimental.SuperBuilder
    @Schema(name = "MemberDto.SearchReq", description = "회원 검색 및 페이징 요청")
    public static class SearchReq extends PageDto.PageReq {

        @Schema(description = "회원 이름 검색 키워드", example = "길동")
        private String memberNm;

        @Schema(description = "회원 전화번호(평문, 서비스에서 hash 변환 후 검색)", example = "01012345678")
        private String memberPhone; // 평문 입력 → 서비스에서 hash 변환 검색

        @Schema(description = "CI(평문, 서비스에서 hash 변환 후 검색)")
        private String memberCi;    // 평문 입력 → 서비스에서 hash 변환 검색

        @Schema(description = "DI(평문, 서비스에서 hash 변환 후 검색)")
        private String memberDi;    // 평문 입력 → 서비스에서 hash 변환 검색

        @Schema(description = "회원 상태 (1: 정상, 2: 삭제 등)", example = "1")
        private MemberStatus memberStatus;

        @Schema(description = "개인정보 수집/이용 동의 여부 (Y/N)", example = "Y")
        @Pattern(regexp = "Y|N")
        private String privConsentYn;

        @Schema(description = "문자 수신 동의 여부 (Y/N)", example = "N")
        @Pattern(regexp = "Y|N")
        private String msgConsentYn;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "MemberDto.Res", description = "회원 응답 DTO (마스킹 여부 선택 가능)")
    public static class Res {

        @Schema(description = "회원 SEQ", example = "1")
        private Long memberSeq;

        @Schema(description = "회원 이름", example = "홍길동")
        private String memberNm;

        @Schema(description = "회원 전화번호 (마스킹)", example = "010****5678")
        private String memberPhone;

        @Schema(description = "회원 상태 (1: 정상, 2: 삭제 등)", example = "1")
        private MemberStatus memberStatus;

        @Schema(description = "개인정보 수집/이용 동의 여부 (Y/N)", example = "Y")
        private String privConsentYn;

        @Schema(description = "문자 수신 동의 여부 (Y/N)", example = "N")
        private String msgConsentYn;



        /** 엔티티 → DTO (마스킹 미적용) */
        public static Res from(Member e) {
            if (e == null) return null;
            return base(e, false);
        }

        /** 엔티티 → DTO (마스킹 적용 여부 선택) */
        public static Res from(Member e, boolean masked) {
            if (e == null) return null;
            return base(e, masked);
        }

        private static Res base(Member e, boolean masked) {
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
                    .memberStatus(e.getMemberStatus())
                    .privConsentYn(e.getPrivConsentYn())
                    .msgConsentYn(e.getMsgConsentYn())

                    .build();
        }
    }

    // 단건 식별자 응답
    @Getter
    @AllArgsConstructor(staticName = "of")
    @Schema(name = "MemberDto.IdResponse", description = "회원 식별자 응답 DTO")
    public static class IdResponse {

        @Schema(description = "회원 SEQ", example = "1")
        private final Long memberSeq;
    }
}

package io.dnrdl12.remittance.entity;

import io.dnrdl12.remittance.comm.crypto.StringCryptoConverter;
import io.dnrdl12.remittance.comm.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * packageName    : io.dnrdl12.remittance.entity
 * fileName       : Accountmember
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    : 계좌 사용자 엔티티 (회원 정보)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI            최초 생성
 */
@Entity
@Table(name = "member")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "회원 seq")
    @Column(name = "member_seq")
    private Long memberSeq;

    @Schema(description = "회원 이름")
    @Column(name = "member_nm", nullable = false, length = 100)
    private String memberNm;

    @Schema(description = "전화번호")
    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "member_phone", nullable = false, length = 512)
    private String memberPhone;

    @Schema(description = "전화번호 검색용")
    @Column(name = "member_phone_hash", length = 88)
    private String memberPhoneHash;

    @Schema(description = "CI")
    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "member_ci", nullable = false, length = 512)
    private String memberCi;

    @Schema(description = "CI 검색용")
    @Column(name = "member_ci_hash", length = 88)
    private String memberCiHash;

    @Schema(description = "DI")
    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "member_di", nullable = false, length = 512)
    private String memberDi;

    @Schema(description = "DI 검색용")
    @Column(name = "member_di_hash", length = 88)
    private String memberDiHash;

    @Schema(description = "계정 상태: 1 정상, 2 삭제")
    @Column(name = "member_status", nullable = false)
    private Integer memberStatus = 1;

    @Schema(description = "개인정보 동의 여부")
    @Column(name = "priv_consent_yn", nullable = false, length = 1)
    private String privConsentYn = "Y";

    @Schema(description = "문자 수신 동의 여부")
    @Column(name = "msg_consent_yn", nullable = false, length = 1)
    private String msgConsentYn = "Y";
}

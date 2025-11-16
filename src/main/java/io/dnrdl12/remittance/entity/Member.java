package io.dnrdl12.remittance.entity;

import io.dnrdl12.remittance.comm.crypto.StringCryptoConverter;
import io.dnrdl12.remittance.comm.entity.BaseEntity;
import io.dnrdl12.remittance.comm.enums.MemberStatus;
import io.dnrdl12.remittance.converter.MemberStatusConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * packageName    : io.dnrdl12.remittance.entity
 * fileName       : Member
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    : 계좌 사용자 엔티티 (회원 정보)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI            최초 생성
 * 2025-11-15        JW.CHOI            컬럼 코멘트 추가, 조인관련 수정
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
    @Comment("회원 고유 식별자")
    @Column(name = "member_seq")
    private Long memberSeq;

    @Schema(description = "회원 이름")
    @Comment("회원 이름")
    @Column(name = "member_nm", nullable = false, length = 100)
    private String memberNm;

    @Schema(description = "전화번호(암호화 저장)")
    @Comment("전화번호 (AES256 암호화 저장)")
    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "member_phone", nullable = false, length = 512)
    private String memberPhone;

    @Schema(description = "전화번호 해시 (정확 검색용)")
    @Comment("전화번호 해시 (정확 검색용: HMAC-SHA256)")
    @Column(name = "member_phone_hash", length = 88)
    private String memberPhoneHash;

    @Schema(description = "CI(암호화 저장)")
    @Comment("CI (AES256 암호화 저장)")
    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "member_ci", nullable = false, length = 512)
    private String memberCi;

    @Schema(description = "CI 해시 (정확 검색용)")
    @Comment("CI 해시 (정확 검색용: HMAC-SHA256)")
    @Column(name = "member_ci_hash", length = 88)
    private String memberCiHash;

    @Schema(description = "DI(암호화 저장)")
    @Comment("DI (AES256 암호화 저장)")
    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "member_di", nullable = false, length = 512)
    private String memberDi;

    @Schema(description = "DI 해시 (정확 검색용)")
    @Comment("DI 해시 (정확 검색용: HMAC-SHA256)")
    @Column(name = "member_di_hash", length = 88)
    private String memberDiHash;

    @Schema(description = "계정 상태: 1 정상, 2 삭제")
    @Comment("계정 상태 (1 정상, 2 삭제)")
    @Convert(converter = MemberStatusConverter.class)
    @Column(name = "member_status", nullable = false)
    private MemberStatus memberStatus = MemberStatus.ACTIVE;

    @Schema(description = "개인정보 동의 여부")
    @Comment("개인정보 처리 동의 여부 (Y/N)")
    @Column(name = "priv_consent_yn", nullable = false, length = 1)
    private String privConsentYn = "Y";

    @Schema(description = "문자 수신 동의 여부")
    @Comment("문자 수신 동의 여부 (Y/N)")
    @Column(name = "msg_consent_yn", nullable = false, length = 1)
    private String msgConsentYn = "Y";

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();
}

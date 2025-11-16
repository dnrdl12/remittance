package io.dnrdl12.remittance.comm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : io.dnrdl12.remittance.comm.enums
 * fileName       : MemberStatus
 * author         : JW.CHOI
 * date           : 2025-11-15
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-15        JW.CHOI              최초 생성
 */
@Getter
@RequiredArgsConstructor
public enum MemberStatus implements CodeEnum<Integer> {
    ACTIVE(1, "정상"),   // 정상
    DELETED(2, "삭제");  // 삭제

    private final Integer code;
    private final String description;

    public static MemberStatus fromCode(Integer code) {
        if (code == null) return null;
        for (MemberStatus type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 계좌 종류 코드: " + code);
    }
}
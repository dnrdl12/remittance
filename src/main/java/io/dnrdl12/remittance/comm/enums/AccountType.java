package io.dnrdl12.remittance.comm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : io.dnrdl12.remittance.comm.enums
 * fileName       : AccountType
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */

@Getter
@RequiredArgsConstructor
public enum AccountType implements CodeEnum<Integer> {

    NORMAL(1, "일반"),
    SALARY(2, "월급통장"),
    LIMIT(3, "한도통장");

    private final Integer code;
    private final String description;

    public static AccountType fromCode(Integer code) {
        if (code == null) return null;
        for (AccountType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 계좌 종류 코드: " + code);
    }
}

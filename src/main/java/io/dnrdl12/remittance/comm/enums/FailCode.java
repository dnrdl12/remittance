package io.dnrdl12.remittance.comm.enums;

/**
 * packageName    : io.dnrdl12.remittance.comm.enums
 * fileName       : FailCode
 * author         : JW.CHOI
 * date           : 2025-11-15
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-15        JW.CHOI              최초 생성
 */
public enum FailCode {

    INSUFFICIENT_BALANCE("잔액 부족"),
    ACCOUNT_SUSPENDED("계좌 정지"),
    ACCOUNT_CLOSED("계좌 해지"),
    INVALID_ACCOUNT("유효하지 않은 계좌"),
    LIMIT_EXCEEDED("한도 초과"),
    ACCOUNT_STATUS_INVALID(""),
    SYSTEM_ERROR("시스템 오류");

    private final String description;

    FailCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static FailCode fromCode(String code) {
        for (FailCode fc : FailCode.values()) {
            if (fc.name().equalsIgnoreCase(code)) {
                return fc;
            }
        }
        throw new IllegalArgumentException("Invalid FailCode: " + code);
    }
}

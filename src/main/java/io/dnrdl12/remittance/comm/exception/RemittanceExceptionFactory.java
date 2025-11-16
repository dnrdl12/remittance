package io.dnrdl12.remittance.comm.exception;

import io.dnrdl12.remittance.comm.enums.ErrorCode;

/**
 * packageName    : io.dnrdl12.remittance.comm.exception
 * fileName       : RemittanceExceptionFactory
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */
public class RemittanceExceptionFactory {
    public static RemittanceException of(ErrorCode errorCode) {
        return new RemittanceException(errorCode);
    }

    public static RemittanceException of(ErrorCode errorCode, String detailMessage) {
        return new RemittanceException(errorCode, detailMessage);
    }
}
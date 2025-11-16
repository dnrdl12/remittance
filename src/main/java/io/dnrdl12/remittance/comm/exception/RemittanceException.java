package io.dnrdl12.remittance.comm.exception;

import io.dnrdl12.remittance.comm.enums.ErrorCode;
import io.dnrdl12.remittance.comm.enums.ResultCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * packageName    : io.dnrdl12.remittance.comm.exception
 * fileName       : RemittanceException
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */
@Getter
public class RemittanceException extends RuntimeException {

    private final ErrorCode errorCode;

    public RemittanceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public RemittanceException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    public String getResultCode() {
        return errorCode.getResultCode();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getStatus();
    }
}
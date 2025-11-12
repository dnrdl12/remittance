package io.dnrdl12.remittance.comm.exception;

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
    private final ResultCode resultCode;
    private final HttpStatus httpStatus;

    public RemittanceException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public RemittanceException(ResultCode resultCode, String message, HttpStatus httpStatus) {
        super(message);
        this.resultCode = resultCode;
        this.httpStatus = httpStatus;
    }
}
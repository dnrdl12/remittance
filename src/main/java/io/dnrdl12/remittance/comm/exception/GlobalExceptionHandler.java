package io.dnrdl12.remittance.comm.exception;

import io.dnrdl12.remittance.comm.api.BaseResponse;
import io.dnrdl12.remittance.comm.enums.ResultCode;
import io.dnrdl12.remittance.controller.AccountController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * packageName    : io.dnrdl12.remittance.api.response
 * fileName       : GlobalExceptionHandler
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    : 공통 오류 핸들러 (항상 BaseResponse 형태로 응답)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI            최초 생성
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 예상 못한 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleAll(Exception ex, HttpServletRequest req) {
        printException(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.<Void>fail(ResultCode.INTERNAL_ERROR, ex.getMessage())
                        .withPath(req.getRequestURI()));
    }

    /** @Valid 바인딩 오류 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                            HttpServletRequest req) {
        printException(ex);
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> String.format("[ %s ] 필드를 확인해주세요. 입력된 값: [ %s ]",
                        fe.getField(), fe.getRejectedValue()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.fail(ResultCode.BAD_REQUEST, String.join("; ", errors))
                        .withPath(req.getRequestURI()));
    }

    /** Bean Validation (메서드 파라미터 제약 위반) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(ConstraintViolationException ex,
                                                                       HttpServletRequest req) {
        printException(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.<Void>fail(ResultCode.BAD_REQUEST, ex.getMessage())
                        .withPath(req.getRequestURI()));
    }

    /** 필수 파라미터 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex,
                                                                HttpServletRequest req) {
        printException(ex);
        String msg = String.format("[ %s ]는 필수값 입니다.", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.<Void>fail(ResultCode.BAD_REQUEST, msg)
                        .withPath(req.getRequestURI()));
    }

    /** 파라미터 타입 불일치 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest req) {
        printException(ex);
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알수없음";
        String msg = String.format("[ %s ]는 %s 타입만 가능합니다.", ex.getName(), required);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.<Void>fail(ResultCode.BAD_REQUEST, msg)
                        .withPath(req.getRequestURI()));
    }

    /** 비즈니스 예외 (예: 멱등성 충돌, 잔액부족 등) */
    @ExceptionHandler(RemittanceException.class)
    public ResponseEntity<BaseResponse<Void>> handleBiz(RemittanceException ex, HttpServletRequest req) {
        printException(ex);
        var status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(BaseResponse.<Void>fail(ex.getResultCode(), ex.getMessage()).withPath(req.getRequestURI()));
    }

    private void printException(Exception ex) {
        log.error("====================================================");
        log.error("[Exception] {}", ex.toString(), ex);
        log.error("====================================================");
    }
}

package io.dnrdl12.remittance.comm.exception;

import io.dnrdl12.remittance.comm.api.BaseResponse;
import io.dnrdl12.remittance.comm.enums.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    // =============================================================
    // 1. 예상 못한 모든 예외
    // =============================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleAll(Exception ex, HttpServletRequest req) {
        printException(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.<Void>fail( ResultCode.INTERNAL_ERROR, null, ex.getMessage()).withPath(req.getRequestURI()));
    }

    // =============================================================
    // 2. @Valid 바인딩 오류 (DTO 필드 검증 실패)
    // =============================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        printException(ex);
        String msg = "[%s] 필드를 확인해주세요. 입력된 값: [%s]. 원인: %s";
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> String.format( msg, fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage() ))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body( BaseResponse.fail( ResultCode.BAD_REQUEST, null, String.join("; ", errors) ).withPath(req.getRequestURI()));
    }

    // =============================================================
    // 3. Bean Validation (@RequestParam, @PathVariable 검증 실패)
    // =============================================================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        printException(ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body( BaseResponse.<Void>fail( ResultCode.BAD_REQUEST, null, ex.getMessage() ).withPath(req.getRequestURI()) );
    }

    // =============================================================
    // 4. 필수 파라미터 누락
    // =============================================================
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        printException(ex);

        String msg = String.format("[ %s ]는 필수값 입니다.", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body( BaseResponse.<Void>fail( ResultCode.BAD_REQUEST, null, msg ).withPath(req.getRequestURI()) );
    }

    // =============================================================
    // 5. 파라미터 타입 불일치
    // =============================================================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        printException(ex);

        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알수없음";
        String msg = String.format("[ %s ]는 %s 타입만 가능합니다.", ex.getName(), required);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body( BaseResponse.<Void>fail( ResultCode.BAD_REQUEST, null, msg ).withPath(req.getRequestURI()) );
    }

    // =============================================================
    // 6. 비즈니스 예외 (RemittanceException)
    // =============================================================
    @ExceptionHandler(RemittanceException.class)
    public ResponseEntity<BaseResponse<Void>> handleBiz(RemittanceException ex, HttpServletRequest req) {
        printException(ex);
        var status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body( BaseResponse.<Void>fail(ex.getErrorCode()).withPath(req.getRequestURI()) );
    }

    // =============================================================
    // 공통 로그
    // =============================================================
    private void printException(Exception ex) {
        log.error("====================================================");
        log.error("[Exception] {}", ex.toString(), ex);
        log.error("====================================================");
    }
}

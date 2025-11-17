package io.dnrdl12.remittance.comm.enums;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * packageName    : io.dnrdl12.remittance.comm.enums
 * fileName       : ErrorCode
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHmport lombok.Getter;
 * import org.springframework.http.HttpStatus;OI              최초 생성
 */
@Getter
public enum ErrorCode {

    USER_ALREADY_EXISTS("M001", "이미 등록된 회원입니다.", HttpStatus.CONFLICT),
    INVALID_USER_NAME("M002", "회원 이름 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER("M003", "전화번호 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_CI("M004", "CI 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_CI("M005", "이미 동일한 CI로 등록된 회원이 존재합니다.", HttpStatus.CONFLICT),
    MISSING_REQUIRED_FIELDS("M006", "필수 입력값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    USER_REGISTRATION_FAILED("M007", "회원 등록에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("M008", "사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    USER_ALREADY_DELETED("M009", "이미 삭제된 회원입니다.", HttpStatus.GONE),

    INVALID_AMOUNT("E001", "금액이 0보다 커야 합니다.", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_FOUND("E002", "계좌를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE("E003", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    TRANSFER_SAME_ACCOUNT("E004", "출금 계좌와 입금 계좌가 동일합니다.", HttpStatus.BAD_REQUEST),
    AMOUNT_ALREADY_DELETED("E005", "이미 삭제된 계좌 입니다.", HttpStatus.NOT_FOUND),
    AMOUNT_DELETED("E006", "삭제 된 계좌 입니다.", HttpStatus.NOT_FOUND),
    AMOUNT_SUSPENDED("E007", "정지 된 계좌 입니다.", HttpStatus.NOT_FOUND),

    FEE_POLICY_NOT_SELECT("F002", "수수료 정책을 선택해주세요.", HttpStatus.NOT_FOUND),
    FEE_POLICY_NOT_FOUND("F001", "해당 수수료 정책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    INVALID_DEPOSIT_AMOUNT("T001", "입금 금액은 0보다 커야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_WITHDRAW_AMOUNT("T002", "출금 금액은 0보다 커야 합니다.", HttpStatus.BAD_REQUEST),
    IDEMPOTENCY_KEY_USED_DIFFERENT_PARAMS("T0003", "이미 사용된 멱등키입니다. 요청 파라미터가 기존 요청과 다릅니다.", HttpStatus.BAD_REQUEST),
    CUSTOM_ERROR("C000", "관리자에게 문의가 필요합니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String resultCode;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String resultCode, String message, HttpStatus status) {
        this.resultCode = resultCode;
        this.message = message;
        this.status = status;
    }
}
package io.dnrdl12.remittance.comm.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * packageName    : io.dnrdl12.remittance.comm.enums
 * fileName       : ResultCode
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */
@Schema(description = "표준 결과 코드")
@Getter
public enum ResultCode {

    OK("S0000", "성공"),
    CREATED("S0001", "생성됨"),
    ACCEPTED("S0002", "요청 접수됨"),

    // 유효성/요청 오류
    BAD_REQUEST("E4000", "잘못된 요청입니다"),
    UNAUTHORIZED("E4010", "인증 필요"),
    FORBIDDEN("E4030", "접근 권한 없음"),
    NOT_FOUND("E4040", "리소스를 찾을 수 없습니다"),
    CONFLICT("E4090", "중복/충돌"),

    // 서버/시스템
    INTERNAL_ERROR("E5000", "시스템 오류가 발생했습니다"),
    SERVICE_UNAVAILABLE("E5030", "서비스 사용 불가"),

    // 도메인(예시: 이체)
    TRANSFER_DUPLICATED("T1001", "중복된 이체 요청입니다"),
    TRANSFER_INSUFFICIENT_BALANCE("T1002", "잔액 부족"),
    TRANSFER_ACCOUNT_FROZEN("T1003", "계좌 정지 상태입니다");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
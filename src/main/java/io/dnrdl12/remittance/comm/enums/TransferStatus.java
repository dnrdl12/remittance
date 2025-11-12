package io.dnrdl12.remittance.comm.enums;

/**
 * packageName    : io.dnrdl12.remittance.common.enums
 * fileName       : TransferStatus
 * author         : JW.CHOI
 * date           : 2025-11-11
 * description    : 이체 상태 코드
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-11        JW.CHOI            최초 생성
 */
public enum TransferStatus {
    PENDING,     // 처리 대기
    POSTED,      // 정상 완료
    FAILED,      // 실패
    CANCELLED    // 취소
}
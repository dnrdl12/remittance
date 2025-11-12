package io.dnrdl12.remittance.comm.enums;

/**
 * packageName    : io.dnrdl12.remittance.comm.enums
 * fileName       : EntryType
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    : 거래 원장 분개 유형 (DEBIT / CREDIT)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI            최초 생성
 */
public enum EntryType {
    DEBIT,   // 차변 (출금 / 자산 감소)
    CREDIT   // 대변 (입금 / 자산 증가)
}

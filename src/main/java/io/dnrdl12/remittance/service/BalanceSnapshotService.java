package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.entity.Account;

/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : BalanceSnapshotService
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */
public interface BalanceSnapshotService  {
    void initForAccount(Account account);
    long getCurrentBalance(Long accountSeq);
    void applyDelta(Long accountSeq, long delta);
}
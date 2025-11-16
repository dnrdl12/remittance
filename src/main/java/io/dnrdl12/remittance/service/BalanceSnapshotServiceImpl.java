package io.dnrdl12.remittance.service;

import io.dnrdl12.remittance.entity.Account;
import io.dnrdl12.remittance.entity.BalanceSnapshot;
import io.dnrdl12.remittance.repository.BalanceSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * packageName    : io.dnrdl12.remittance.service
 * fileName       : BalanceSnapshotServiceImpl
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BalanceSnapshotServiceImpl implements BalanceSnapshotService {
    private final BalanceSnapshotRepository snapshotRepository;


    public void initForAccount(Account account) {
        BalanceSnapshot snapshot = BalanceSnapshot.builder()
                .account(account)
                .balance(0L)
                .build();
        account.setRegId(account.getRegId());
        account.setModId(account.getRegId());
        snapshotRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCurrentBalance(Long accountSeq) {
        return snapshotRepository.findById(accountSeq)
                .map(BalanceSnapshot::getBalance)
                .orElse(0L);
    }

    @Override
    @Transactional
    public void applyDelta(Long accountSeq, long delta) {
        BalanceSnapshot snapshot = snapshotRepository.findById(accountSeq)
                .orElseGet(() -> {
                    BalanceSnapshot s = new BalanceSnapshot();
                    s.setAccountSeq(accountSeq);
                    s.setBalance(0L);
                    return s;
                });

        snapshot.setBalance(snapshot.getBalance() + delta);
        snapshotRepository.save(snapshot);
    }
}
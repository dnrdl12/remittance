package io.dnrdl12.remittance.projection;

import io.dnrdl12.remittance.comm.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * packageName    : io.dnrdl12.remittance.projection
 * fileName       : AccountDetailView
 * author         : JW.CHOI
 * date           : 2025-11-15
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-15        JW.CHOI              최초 생성
 */

public interface AccountDetailView {

    Long getAccountSeq();
    String getAccountNumber();
    String getNickname();
    Integer getAccountStatus();
    AccountType getAccountType();
    String getBankCode();
    String getBranchCode();
    LocalDateTime getCreatedDate();
    LocalDateTime getClosedDate();

    MemberPart getMember();
    BalancePart getBalanceSnapshot();
    FeePolicyPart getFeePolicy();

    interface MemberPart {
        String getMemberNm();
        String getMemberPhone();
        Integer getMemberStatus();
        String getPrivConsentYn();
        String getMsgConsentYn();
    }

    interface BalancePart {
        BigDecimal getBalance();
    }

    interface FeePolicyPart {
        String getPolicyName();
        BigDecimal getTransferFeeRate();
        BigDecimal getWithdrawFeeRate();
    }
}
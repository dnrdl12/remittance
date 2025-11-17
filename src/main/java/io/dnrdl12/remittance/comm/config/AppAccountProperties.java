package io.dnrdl12.remittance.comm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * packageName    : io.dnrdl12.remittance.comm.config
 * fileName       : AppAccountProperties
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    : AccountConstants 대체 프로퍼티관리 yml사용
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.account")
public class AppAccountProperties {
    private Long systemAccountSeq;
    private Long feeAccountSeq;
    private Integer defaultFeePolicySeq;
    private String defaultBankCode;
    private String defaultBranchCode;
    private Long defaultDailyTransferLimit;
    private Long defaultDailyWithdrawLimit;
    private String defaultCurrency;
}
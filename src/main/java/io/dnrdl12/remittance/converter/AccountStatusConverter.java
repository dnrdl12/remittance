package io.dnrdl12.remittance.converter;

import io.dnrdl12.remittance.comm.enums.AccountStatus;
import jakarta.persistence.Converter;

/**
 * packageName    : io.dnrdl12.remittance.converter
 * fileName       : AccountStatusConverter
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */
@Converter(autoApply = false)
public class AccountStatusConverter extends AbstractCodeEnumConverter<AccountStatus> {

    public AccountStatusConverter() {
        super(AccountStatus.class);
    }
}
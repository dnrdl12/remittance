package io.dnrdl12.remittance.converter;

import io.dnrdl12.remittance.comm.enums.AccountType;
import jakarta.persistence.Converter;
/**
 * packageName    : io.dnrdl12.remittance.converter
 * fileName       : AccountTypeConverter
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */
@Converter(autoApply = false)
public class AccountTypeConverter extends AbstractCodeEnumConverter<AccountType> {

    public AccountTypeConverter() {
        super(AccountType.class);
    }
}
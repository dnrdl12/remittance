package io.dnrdl12.remittance.comm.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
/**
 * packageName    : io.dnrdl12.remittance.comm.crypto
 * fileName       : StringCryptoConverter
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */
@Converter
public class StringCryptoConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) return null;
        return BeanProvider.getBean(CryptoUtil.class).encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        return BeanProvider.getBean(CryptoUtil.class).decrypt(dbData);
    }
}
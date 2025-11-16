package io.dnrdl12.remittance.converter;

import io.dnrdl12.remittance.comm.enums.CodeEnum;
import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;
/**
 * packageName    : io.dnrdl12.remittance.converter
 * fileName       : AbstractCodeEnumConverter
 * author         : JW.CHOI
 * date           : 2025-11-16
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-16        JW.CHOI              최초 생성
 */
@RequiredArgsConstructor
public abstract class AbstractCodeEnumConverter<E extends Enum<E> & CodeEnum<Integer>>
        implements AttributeConverter<E, Integer> {

    private final Class<E> targetEnumClass;

    @Override
    public Integer convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public E convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;

        for (E e : targetEnumClass.getEnumConstants()) {
            if (e.getCode().equals(dbData)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                targetEnumClass.getSimpleName() + " 코드 매핑 실패: " + dbData
        );
    }
}
package com.endside.user.model;

import com.endside.user.constants.Os;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class OsConverter implements AttributeConverter<Os,Integer> {

    @Override
    public Integer convertToDatabaseColumn(Os os) {
        if (os == null) {
            return null;
        }
        return os.getTypeNum();
    }

    @Override
    public Os convertToEntityAttribute(Integer typeNum) {
        if (typeNum == null) {
            return null;
        }
        return Stream.of(Os.values())
                .filter(c -> c.getTypeNum() == typeNum)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}

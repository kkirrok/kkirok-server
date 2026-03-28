package com.kkirok.server.domain.member.util;

import com.kkirok.server.domain.member.domain.OnboardingHabit;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Converter
public class OnboardingHabitListConverter implements AttributeConverter<List<OnboardingHabit>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(final List<OnboardingHabit> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }

        return attribute.stream()
                .map(OnboardingHabit::name)
                .reduce((left, right) -> left + DELIMITER + right)
                .orElse(null);
    }

    @Override
    public List<OnboardingHabit> convertToEntityAttribute(final String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(dbData.split(DELIMITER))
                .map(String::trim)
                .map(OnboardingHabit::valueOf)
                .toList();
    }
}

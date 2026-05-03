package com.kkirok.server.domain.meal.util;

import com.kkirok.server.domain.meal.domain.MealNutrition;
import com.kkirok.server.domain.meal.domain.MealRecord;

import java.util.List;
import java.util.Objects;
import java.util.function.ToLongFunction;

public class NutrientAvgUtil {

    public static int getAvg(List<MealRecord> mealRecords, ToLongFunction<MealNutrition> extractor) {
        return (int) mealRecords.stream()
                .map(MealRecord::getMealNutrition)
                .filter(Objects::nonNull)
                .mapToLong(extractor)
                .average()
                .orElse(0);
    }
}
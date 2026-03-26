package com.kkirok.server.domain.meal.util;

import com.kkirok.server.domain.meal.domain.MealAiAnalysis;
import com.kkirok.server.domain.meal.domain.MealRecord;

import java.util.List;
import java.util.function.ToLongFunction;

public class NutrientMaxUtil {

    public static int getMax(List<MealRecord> mealRecords, ToLongFunction<MealAiAnalysis> extractor) {
        long max = 0L;

        for (MealRecord mealRecord : mealRecords) {
            for (MealAiAnalysis mealAiAnalysis : mealRecord.getMealAiAnalyses()) {
                max = Math.max(max, extractor.applyAsLong(mealAiAnalysis));
            }
        }

        return (int) max;
    }
}

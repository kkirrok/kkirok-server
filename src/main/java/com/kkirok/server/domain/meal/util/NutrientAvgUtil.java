package com.kkirok.server.domain.meal.util;

import com.kkirok.server.domain.meal.domain.MealAiAnalysis;
import com.kkirok.server.domain.meal.domain.MealRecord;

import java.util.List;
import java.util.function.ToLongFunction;

public class NutrientAvgUtil {

    public static int getAvg(List<MealRecord> mealRecords, ToLongFunction<MealAiAnalysis> extractor) {

        long sum = 0L;
        int count = 0;

        for (MealRecord mealRecord : mealRecords) {
            for (MealAiAnalysis mealAiAnalysis : mealRecord.getMealAiAnalyses()) {
                sum += extractor.applyAsLong(mealAiAnalysis);
                count++;
            }
        }

        return count > 0 ? (int) (sum / count) : 0;
    }
}

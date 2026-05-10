package com.kkirok.server.global.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;

/**
 * OpenAI Vision API 음식 분석 응답 DTO.
 *
 * snake_case 응답과 camelCase 응답을 모두 받을 수 있도록 JsonAlias를 사용합니다.
 */
public record OpenAiFoodAnalysisResult(

        @JsonProperty("foodName")
        @JsonAlias({"detected_food_name", "food_name"})
        String foodName,

        @JsonProperty("foodCategory")
        @JsonAlias("food_category")
        String foodCategory,

        @JsonProperty("mealTimeSlot")
        @JsonAlias("meal_time_slot")
        MealTimeSlot mealTimeSlot,

        @JsonProperty("kcal")
        Integer kcal,

        @JsonProperty("proteinG")
        @JsonAlias("protein_g")
        Double proteinG,

        @JsonProperty("carbohydrateG")
        @JsonAlias("carbohydrate_g")
        Double carbohydrateG,

        @JsonProperty("sugarG")
        @JsonAlias("sugar_g")
        Double sugarG,

        @JsonProperty("fatG")
        @JsonAlias("fat_g")
        Double fatG,

        @JsonProperty("sodiumMg")
        @JsonAlias("sodium_mg")
        Double sodiumMg,

        @JsonProperty("mealSummary")
        @JsonAlias("meal_summary")
        String mealSummary,

        @JsonProperty("nutritionSummary")
        @JsonAlias("nutrition_summary")
        String nutritionSummary,

        @JsonProperty("positivePoint")
        @JsonAlias("positive_point")
        String positivePoint,

        @JsonProperty("improvementSuggestion")
        @JsonAlias("improvement_suggestion")
        String improvementSuggestion,

        @JsonProperty("rawResultJson")
        @JsonAlias("raw_result_json")
        String rawResultJson
) {
    public int kcalOrDefault() {
        return kcal != null ? kcal : 0;
    }

    public double proteinOrDefault() {
        return proteinG != null ? proteinG : 0.0;
    }

    public double carbohydrateOrDefault() {
        return carbohydrateG != null ? carbohydrateG : 0.0;
    }

    public double sugarOrDefault() {
        return sugarG != null ? sugarG : 0.0;
    }

    public double fatOrDefault() {
        return fatG != null ? fatG : 0.0;
    }

    public double sodiumOrDefault() {
        return sodiumMg != null ? sodiumMg : 0.0;
    }

    public String foodNameOrDefault() {
        return foodName != null && !foodName.isBlank()
                ? foodName
                : "알 수 없는 음식";
    }

    public String foodCategoryOrDefault() {
        return foodCategory != null && !foodCategory.isBlank()
                ? foodCategory
                : "기타";
    }

    public String nutritionSummaryOrDefault() {
        if (nutritionSummary != null && !nutritionSummary.isBlank()) {
            return nutritionSummary;
        }

        if (mealSummary != null && !mealSummary.isBlank()) {
            return mealSummary;
        }

        return "";
    }

    public String rawResultJsonOrDefault() {
        return rawResultJson != null ? rawResultJson : "";
    }

    public MealTimeSlot mealTimeSlotOrDefault() {
        return mealTimeSlot != null ? mealTimeSlot : MealTimeSlot.BREAKFAST;
    }
}
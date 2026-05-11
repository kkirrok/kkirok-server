package com.kkirok.server.domain.meal.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MealRecommendationRequest(

        @JsonProperty("totalKcal")
        int totalKcal,

        @JsonProperty("totalProteinG")
        double totalProteinG,

        @JsonProperty("totalCarbohydrateG")
        double totalCarbohydrateG,

        @JsonProperty("totalFatG")
        double totalFatG,

        @JsonProperty("totalSodiumMg")
        double totalSodiumMg,

        @JsonProperty("mealCount")
        int mealCount
) {
}

package com.kkirok.server.global.external.publicdata.dto;

import lombok.Builder;

@Builder
public record FoodNutritionSearchResult(
        String foodName,
        String manufacturer,   // 가공식품만 있음, 일반식품은 null
        Integer kcal,
        Double carbohydrateG,
        Double proteinG,
        Double fatG,
        Double sugarG,
        Double sodiumMg,
        FoodSourceType sourceType  // PROCESSED, STANDARD
) {}
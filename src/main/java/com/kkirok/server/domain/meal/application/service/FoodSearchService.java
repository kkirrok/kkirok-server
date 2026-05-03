package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.global.external.publicdata.PublicDataFoodClient;
import com.kkirok.server.global.external.publicdata.dto.FoodNutritionSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class FoodSearchService {

    private final PublicDataFoodClient publicDataFoodClient;

    public List<FoodNutritionSearchResult> search(String keyword) {
        // 가공식품 + 일반식품 병렬 조회 후 합산
        List<FoodNutritionSearchResult> processed = publicDataFoodClient.searchProcessedFood(keyword);
        List<FoodNutritionSearchResult> standard  = publicDataFoodClient.searchStandardFood(keyword);

        return Stream.concat(processed.stream(), standard.stream()).toList();
    }
}
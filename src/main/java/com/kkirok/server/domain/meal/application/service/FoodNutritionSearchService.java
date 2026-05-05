package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.global.external.publicdata.PublicDataFoodClient;
import com.kkirok.server.global.external.publicdata.dto.FoodNutritionSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class FoodNutritionSearchService {

    private final PublicDataFoodClient publicDataFoodClient;

    public List<FoodNutritionSearchResult> search(String keyword) {
        // 1. 삼각김밥 같은 가공식품은 가공식품 API에서 먼저 찾기
        List<FoodNutritionSearchResult> processedResults =
                trySearch(() -> publicDataFoodClient.searchProcessedFood(keyword));

        List<FoodNutritionSearchResult> filteredProcessedResults =
                filterByKeyword(processedResults, keyword);

        if (!filteredProcessedResults.isEmpty()) {
            return filteredProcessedResults;
        }

        // 2. 가공식품에서 못 찾으면 일반식품 API에서 찾기
        List<FoodNutritionSearchResult> standardResults =
                trySearch(() -> publicDataFoodClient.searchStandardFood(keyword));

        return filterByKeyword(standardResults, keyword);
    }

    private List<FoodNutritionSearchResult> filterByKeyword(
            List<FoodNutritionSearchResult> results,
            String keyword
    ) {
        String normalizedKeyword = normalize(keyword);

        return results.stream()
                .filter(result -> result.foodName() != null)
                .filter(result -> normalize(result.foodName()).contains(normalizedKeyword))
                .toList();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace(" ", "")
                .replace("_", "")
                .trim();
    }

    private List<FoodNutritionSearchResult> trySearch(
            Supplier<List<FoodNutritionSearchResult>> supplier
    ) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return List.of();
        }
    }
}
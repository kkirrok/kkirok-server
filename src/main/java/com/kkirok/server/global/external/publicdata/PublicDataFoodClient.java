package com.kkirok.server.global.external.publicdata;

import com.kkirok.server.global.external.publicdata.dto.FoodNutritionSearchResult;
import java.util.List;

public interface PublicDataFoodClient {
    List<FoodNutritionSearchResult> searchProcessedFood(String keyword);  // 가공식품
    List<FoodNutritionSearchResult> searchStandardFood(String keyword);   // 일반식품
}
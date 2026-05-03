package com.kkirok.server.global.external.publicdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.kkirok.server.global.external.publicdata.config.PublicDataFoodProperties;
import com.kkirok.server.global.external.publicdata.dto.FoodNutritionSearchResult;
import com.kkirok.server.global.external.publicdata.dto.FoodSourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PublicDataFoodRestClient implements PublicDataFoodClient {

    // 가공식품 API 경로
    private static final String PROCESSED_PATH =
            "/1471000/FoodNtrCpntDbInfo01/getFoodNtrCpntDbInq01";
    // 일반식품 API 경로
    private static final String STANDARD_PATH =
            "/1470000/FoodNtrIrdntInfoService01/getFoodNtrItdntList01";

    private final RestClient restClient;
    private final PublicDataFoodProperties properties;

    @Override
    public List<FoodNutritionSearchResult> searchProcessedFood(String keyword) {
        JsonNode body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PROCESSED_PATH)
                        .queryParam("serviceKey", properties.getServiceKey())
                        .queryParam("FOOD_NM_KR", keyword)
                        .queryParam("type", "json")
                        .queryParam("numOfRows", 10)
                        .queryParam("pageNo", 1)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        return parseProcessedFood(body);
    }

    @Override
    public List<FoodNutritionSearchResult> searchStandardFood(String keyword) {
        JsonNode body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(STANDARD_PATH)
                        .queryParam("serviceKey", properties.getServiceKey())
                        .queryParam("foodNm", keyword)
                        .queryParam("type", "json")
                        .queryParam("numOfRows", 10)
                        .queryParam("pageNo", 1)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        return parseStandardFood(body);
    }

    private List<FoodNutritionSearchResult> parseProcessedFood(JsonNode body) {
        List<FoodNutritionSearchResult> results = new ArrayList<>();
        if (body == null) return results;

        JsonNode items = body.path("body").path("items");
        if (!items.isArray()) return results;

        for (JsonNode item : items) {
            results.add(FoodNutritionSearchResult.builder()
                    .foodName(getText(item, "FOOD_NM_KR"))
                    .manufacturer(getText(item, "BSSH_NM"))
                    .kcal(getInt(item, "AMT_NUM1"))
                    .carbohydrateG(getDouble(item, "AMT_NUM7"))
                    .proteinG(getDouble(item, "AMT_NUM3"))
                    .fatG(getDouble(item, "AMT_NUM4"))
                    .sugarG(getDouble(item, "AMT_NUM8"))
                    .sodiumMg(getDouble(item, "AMT_NUM6"))
                    .sourceType(FoodSourceType.PROCESSED)
                    .build());
        }
        return results;
    }

    private List<FoodNutritionSearchResult> parseStandardFood(JsonNode body) {
        List<FoodNutritionSearchResult> results = new ArrayList<>();
        if (body == null) return results;

        JsonNode items = body.path("body").path("items");
        if (!items.isArray()) return results;

        for (JsonNode item : items) {
            results.add(FoodNutritionSearchResult.builder()
                    .foodName(getText(item, "foodNm"))
                    .manufacturer(null)
                    .kcal(getInt(item, "enerc"))
                    .carbohydrateG(getDouble(item, "chocdf"))
                    .proteinG(getDouble(item, "prot"))
                    .fatG(getDouble(item, "fatce"))
                    .sugarG(getDouble(item, "sugar"))
                    .sodiumMg(getDouble(item, "nat"))
                    .sourceType(FoodSourceType.STANDARD)
                    .build());
        }
        return results;
    }

    private String getText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asText() : null;
    }

    private Integer getInt(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asInt() : null;
    }

    private Double getDouble(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asDouble() : null;
    }
}
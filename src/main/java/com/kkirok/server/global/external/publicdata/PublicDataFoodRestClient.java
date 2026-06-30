package com.kkirok.server.global.external.publicdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.kkirok.server.global.external.publicdata.config.PublicDataFoodProperties;
import com.kkirok.server.global.external.publicdata.dto.FoodNutritionSearchResult;
import com.kkirok.server.global.external.publicdata.dto.FoodSourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PublicDataFoodRestClient implements PublicDataFoodClient {

    // 가공식품 API 경로
    private static final String PROCESSED_PATH =
            "/openapi/tn_pubr_public_nutri_process_info_api";
    // 일반식품 API 경로
    private static final String STANDARD_PATH =
            "/1471000/FoodNtrCpntDbInfo02/getFoodNtrCpntDbInq02";

    private final RestClient processedRestClient;   // 가공식품용
    private final RestClient standardRestClient;    // 일반식품용
    private final PublicDataFoodProperties properties;

    @Override
    public List<FoodNutritionSearchResult> searchProcessedFood(String keyword) {
        String serviceKey = URLEncoder.encode(
                properties.getServiceKey().trim(),
                StandardCharsets.UTF_8
        );

        String encodedKeyword = URLEncoder.encode(
                keyword,
                StandardCharsets.UTF_8
        );

        String uri = "https://api.data.go.kr"
                + PROCESSED_PATH
                + "?serviceKey=" + serviceKey
                + "&foodNm=" + encodedKeyword
                + "&type=json"
                + "&numOfRows=10"
                + "&pageNo=1";

        JsonNode body = processedRestClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .body(JsonNode.class);

        return parseProcessedFood(body);
    }

    @Override
    public List<FoodNutritionSearchResult> searchStandardFood(String keyword) {
        String serviceKey = URLEncoder.encode(
                properties.getServiceKey().trim(),
                StandardCharsets.UTF_8
        );

        String encodedKeyword = URLEncoder.encode(
                keyword,
                StandardCharsets.UTF_8
        );

        String uri = "https://apis.data.go.kr"
                + STANDARD_PATH
                + "?serviceKey=" + serviceKey
                + "&foodNm=" + encodedKeyword
                + "&type=json"
                + "&numOfRows=10"
                + "&pageNo=1";

        JsonNode body = standardRestClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .body(JsonNode.class);

        return parseStandardFood(body);
    }

    private List<FoodNutritionSearchResult> parseProcessedFood(JsonNode body) {
        List<FoodNutritionSearchResult> results = new ArrayList<>();
        if (body == null) return results;

        JsonNode items = body.path("response").path("body").path("items");
        if (!items.isArray()) return results;

        for (JsonNode item : items) {
            // nutConSrtrQua: 영양소 기준량 (예: "100g"), servSize: 1회 제공량 (예: "210g")
            double baseG = parseWeightG(getText(item, "nutConSrtrQua"), 100.0);
            double servingG = parseWeightG(getText(item, "servSize"), baseG);
            double ratio = servingG / baseG;

            results.add(FoodNutritionSearchResult.builder()
                    .foodName(getText(item, "foodNm"))
                    .manufacturer(getText(item, "mfrNm"))
                    .kcal(scaleInt(getInt(item, "enerc"), ratio))
                    .carbohydrateG(scaleDouble(getDouble(item, "chocdf"), ratio))
                    .proteinG(scaleDouble(getDouble(item, "prot"), ratio))
                    .fatG(scaleDouble(getDouble(item, "fatce"), ratio))
                    .sugarG(scaleDouble(getDouble(item, "sugar"), ratio))
                    .sodiumMg(scaleDouble(getDouble(item, "nat"), ratio))
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
            // AMT_NUM1 등은 100g 기준, SERVING_SIZE: 1회 제공량(g)
            double servingG = parseWeightG(getText(item, "SERVING_SIZE"), 100.0);
            double ratio = servingG / 100.0;

            results.add(FoodNutritionSearchResult.builder()
                    .foodName(getText(item, "FOOD_NM_KR"))
                    .manufacturer(null)
                    .kcal(scaleInt(getInt(item, "AMT_NUM1"), ratio))
                    .carbohydrateG(scaleDouble(getDouble(item, "AMT_NUM6"), ratio))
                    .proteinG(scaleDouble(getDouble(item, "AMT_NUM3"), ratio))
                    .fatG(scaleDouble(getDouble(item, "AMT_NUM4"), ratio))
                    .sugarG(scaleDouble(getDouble(item, "AMT_NUM7"), ratio))
                    .sodiumMg(scaleDouble(getDouble(item, "AMT_NUM13"), ratio))
                    .sourceType(FoodSourceType.STANDARD)
                    .build());
        }

        return results;
    }

    /**
     * "210g", "100ml" 같은 문자열에서 숫자만 파싱. 파싱 실패 시 defaultValue 반환.
     */
    private double parseWeightG(String text, double defaultValue) {
        if (text == null || text.isBlank()) return defaultValue;
        try {
            String digits = text.replaceAll("[^0-9.]", "").trim();
            return digits.isEmpty() ? defaultValue : Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer scaleInt(Integer base, double ratio) {
        if (base == null) return null;
        return (int) Math.round(base * ratio);
    }

    private Double scaleDouble(Double base, double ratio) {
        if (base == null) return null;
        return Math.round(base * ratio * 100.0) / 100.0;
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
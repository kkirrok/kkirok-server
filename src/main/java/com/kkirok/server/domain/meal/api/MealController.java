package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealImageUploadRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.dto.response.RecommendationResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayNutritionSummaryResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayStatusResponse;
import com.kkirok.server.domain.report.application.dto.response.WeeklyReportResponse;
import com.kkirok.server.domain.meal.application.service.FoodNutritionSearchService;
import com.kkirok.server.domain.meal.application.service.RecommendationService;
import com.kkirok.server.domain.report.service.WeeklyReportService;
import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.exception.MealSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.external.publicdata.dto.FoodNutritionSearchResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/meals")
@RequiredArgsConstructor
@RoleUserAuth
public class MealController implements MealApi {

    private final MealRecordUseCase mealRecordUseCase;
    private final FoodNutritionSearchService foodNutritionSearchService;
    private final RecommendationService recommendationService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<List<MealResponse>>> getMeals(
            @CurrentMember Long memberId
    ) {
        List<MealResponse> responses = mealRecordUseCase.getTodayRecords(memberId)
                .stream()
                .map(MealResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                SuccessResponse.of(MealSuccessCode.MEAL_GET_SUCCESS, responses)
        );
    }

    @GetMapping("/nutrition/summary/today")
    public ResponseEntity<SuccessResponse<TodayNutritionSummaryResponse>> getTodayNutritionSummary(
            @CurrentMember Long memberId
    ) {
        TodayNutritionSummaryResponse response =
                mealRecordUseCase.getTodayNutritionSummary(memberId);

        return ResponseEntity.ok(
                SuccessResponse.of(MealSuccessCode.TODAY_NUTRITION_SUMMARY_GET_SUCCESS, response)
        );
    }

    @Override
    @PostMapping(
            value = "/record/camera",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<SuccessResponse<MealResponse>> recordMealByCamera(
            @CurrentMember Long memberId,
            @Valid @ModelAttribute MealImageUploadRequest request
    ) {
        MealResponse response = mealRecordUseCase.createMealByCamera(memberId, request.file());

        return ResponseEntity.ok(
                SuccessResponse.of(MealSuccessCode.MEAL_RECORD_SUCCESS, response)
        );
    }

    @Override
    @PostMapping(
            value = "/record/album",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<SuccessResponse<MealResponse>> recordMealByAlbum(
            @CurrentMember Long memberId,
            @Valid @ModelAttribute MealImageUploadRequest request
    ) {
        MealResponse response = mealRecordUseCase.createMealByAlbum(memberId, request.file());

        return ResponseEntity.ok(
                SuccessResponse.of(MealSuccessCode.MEAL_RECORD_SUCCESS, response)
        );
    }

    @Override
    @PostMapping("/record/manual")
    public ResponseEntity<SuccessResponse<MealResponse>> recordMealManually(
            @CurrentMember Long memberId,
            @Valid @RequestBody MealCreateRequest request
    ) {
        MealResponse response = mealRecordUseCase.create(memberId, request);

        return ResponseEntity.ok(
                SuccessResponse.of(MealSuccessCode.MEAL_RECORD_SUCCESS, response)
        );
    }

    @Override
    @PostMapping("/recommendations")
    public ResponseEntity<SuccessResponse<RecommendationResponse>> getRecommendation(
            @CurrentMember Long memberId
    ) {
        RecommendationResponse response = recommendationService.recommend(memberId);
        return ResponseEntity.ok(SuccessResponse.of(MealSuccessCode.RECOMMENDATION_GET_SUCCESS, response));
    }

    @Override
    @PutMapping("/{mealId}")
    public ResponseEntity<SuccessResponse<MealResponse>> updateMeal(
            @CurrentMember Long memberId,
            @PathVariable("mealId") Long mealId,
            @Valid @RequestBody MealUpdateRequest request
    ) {
        MealResponse response = mealRecordUseCase.updateMeal(memberId, mealId, request);

        return ResponseEntity.ok(
                SuccessResponse.of(MealSuccessCode.MEAL_UPDATE_SUCCESS, response)
        );
    }

    @Override
    @DeleteMapping("/{mealId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMeal(
            @CurrentMember Long memberId,
            @PathVariable("mealId") Long mealId
    ) {
        mealRecordUseCase.deleteMeal(memberId, mealId);

        return ResponseEntity.ok(
                SuccessResponse.from(MealSuccessCode.MEAL_DELETE_SUCCESS)
        );
    }

    @Override
    @GetMapping("/foods/search")
    public ResponseEntity<SuccessResponse<List<FoodNutritionSearchResult>>> searchFood(
            @RequestParam String keyword
    ) {
        List<FoodNutritionSearchResult> results = foodNutritionSearchService.search(keyword);

        return ResponseEntity.ok(
                SuccessResponse.of(MealSuccessCode.FOOD_SEARCH_SUCCESS, results)
        );
    }
}
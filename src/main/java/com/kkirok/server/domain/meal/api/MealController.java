package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.dto.response.RecommendationResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayStatusResponse;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/meals")
@RequiredArgsConstructor
public class MealController implements MealApi {

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<MealResponse>> getMeals(
            @CurrentMember Long memberId
    ) {
        return null;
    }

    @Override
    @GetMapping("/today-status")
    public ResponseEntity<SuccessResponse<TodayStatusResponse>> getTodayStatus(
            @CurrentMember Long memberId
    ) {
        return null;
    }

    @Override
    @PostMapping("/record/camera")
    public ResponseEntity<SuccessResponse<MealResponse>> recordMealByCamera(
            @CurrentMember Long memberId
    ) {
        return null;
    }

    @Override
    @PostMapping("/record/album")
    public ResponseEntity<SuccessResponse<MealResponse>> recordMealByAlbum(
            @CurrentMember Long memberId
    ) {
        return null;
    }

    @Override
    @PostMapping("/record/manual")
    public ResponseEntity<SuccessResponse<MealResponse>> recordMealManually(
            @CurrentMember Long memberId
    ) {
        return null;
    }

    @Override
    @PostMapping("/recommendations")
    public ResponseEntity<SuccessResponse<RecommendationResponse>> recommendMeal(
            @CurrentMember Long memberId
    ) {
        return null;
    }

    @Override
    @PutMapping("/{mealId}")
    public ResponseEntity<SuccessResponse<MealResponse>> updateMeal(
            @CurrentMember Long memberId,
            @PathVariable Long mealId,
            @RequestBody MealUpdateRequest request
            ) {
        return null;
    }

    @Override
    @DeleteMapping("/{mealId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMeal(
            @CurrentMember Long memberId,
            @PathVariable Long mealId
    ) {
        return null;
    }
}
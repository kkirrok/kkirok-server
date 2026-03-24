package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.dto.response.RecommendationResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayStatusResponse;
import com.kkirok.server.domain.meal.exception.MealSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Meal API", description = "식단 관련 API")
public interface MealApi {

    @Operation(
            summary = "오늘의 캐릭터 상태 조회",
            description = """
                    사용자의 오늘 식단 기록 상태를 기반으로 캐릭터 상태를 조회합니다.
                    
                    - 인증된 사용자 기준으로 조회합니다.
                    - 응담: 캐릭터 오늘의 상태(에너지, 건강 정보)
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "TODAY_STATUS_GET_SUCCESS")
    ResponseEntity<SuccessResponse<TodayStatusResponse>> getTodayStatus(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "식단 조회",
            description = """
                    사용자의 식단 기록을 조회합니다.
                    
                    - 인증된 사용자 기준으로 조회합니다.
                    - 응담: 식단 기록(아침, 점심, 저녁, 간식 등의 기록 여부와 영양정보)
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_GET_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> getMeals(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "카메라로 식단 기록",
            description = """
                    카메라로 촬영한 음식 정보를 바탕으로 식단을 기록합니다.
                    
                    - 인증된 사용자 기준으로 기록합니다.
                    - 응답: 식단 기록(아침, 점심, 저녁, 간식 등의 기록 여부와 영양정보)
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_RECORD_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> recordMealByCamera(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "앨범 사진으로 식단 기록",
            description = """
                    앨범에서 선택한 음식 사진 정보를 바탕으로 식단을 기록합니다.
                    
                    
                    - 인증된 사용자 기준으로 기록합니다.
                    - 응답: 식단 기록(아침, 점심, 저녁, 간식 등의 기록 여부와 영양정보)
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_RECORD_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> recordMealByAlbum(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "직접 입력으로 식단 기록",
            description = """
                    사용자가 음식 정보를 직접 입력하여 식단을 기록합니다.
                   
                    - 인증된 사용자 기준으로 기록합니다.
                    - 응답: 식단 기록(아침, 점심, 저녁, 간식 등의 기록 여부와 영양정보)
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_RECORD_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> recordMealManually(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "운동 및 음식 추천",
            description = """
                    사용자의 현재 식단 상태를 기반으로 운동 및 음식 추천 정보를 반환합니다.
                    
                    - 인증된 사용자 기준으로 조회합니다.
                    - 응답: 운동 및 음식 추천 정보
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "RECOMMENDATION_GET_SUCCESS")
    ResponseEntity<SuccessResponse<RecommendationResponse>> recommendMeal(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "식단 수정",
            description = """
                    특정 식단을 수정합니다.
                    
                    - 경로 변수: `mealId`
                    - 응답: 수정된 식단 정보
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_UPDATE_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> updateMeal(
            @CurrentMember Long memberId,
            @PathVariable Long mealId,
            @Valid @RequestBody MealUpdateRequest request
            );

    @Operation(
            summary = "식단 삭제",
            description = """
                    특정 식단 기록을 삭제합니다.
                    
                    - 경로 변수: `mealId`
                    - 응답: 없음
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_DELETE_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> deleteMeal(
            @CurrentMember Long memberId,
            @PathVariable Long mealId
    );
}
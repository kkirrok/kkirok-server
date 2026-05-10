package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealImageUploadRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.dto.response.RecommendationResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayStatusResponse;
import com.kkirok.server.domain.meal.exception.MealErrorCode;
import com.kkirok.server.domain.meal.exception.MealSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.external.publicdata.dto.FoodNutritionSearchResult;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Meal API", description = "식사 기록 관련 API")
public interface MealApi {

    @Operation(
            summary = "오늘 식사 기록 조회 [USER]를 해봅시다~~",
            description = """
                현재 로그인한 사용자의 오늘 식사 기록 목록을 조회합니다.

                - 인증된 사용자 기준으로 조회합니다.
                - 응답: 오늘 식사 기록 목록
                """
    )
    @ApiErrorCodeExamples({})
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_GET_SUCCESS")
    ResponseEntity<SuccessResponse<List<MealResponse>>> getMeals(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "식사 추천 조회 [USER]",
            description = """
                    현재 로그인한 사용자에게 식사 추천 정보를 제공합니다.

                    - 인증된 사용자 기준으로 조회합니다.
                    - 응답: 추천 식단 정보
                    """
    )
    @ApiErrorCodeExamples({})
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_RECOMMENDATION_GET_SUCCESS")
    ResponseEntity<SuccessResponse<RecommendationResponse>> getRecommendation(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId
    );

    @Operation(summary = "오늘의 캐릭터 상태 조회 [USER]")
    @ApiErrorCodeExamples({})
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "TODAY_STATUS_GET_SUCCESS")
    ResponseEntity<SuccessResponse<TodayStatusResponse>> getTodayStatus(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "직접 입력으로 식단 기록 [USER]",
            description = """
                사용자가 음식 정보를 직접 입력하여 식단을 기록합니다.

                - 인증된 사용자 기준으로 기록합니다.
                - 응답: 기록된 식단 정보
                """
    )
    @ApiErrorCodeExamples({})
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_RECORD_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> recordMealManually(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "직접 입력할 식단 정보",
                    required = true
            )
            @Valid @RequestBody MealCreateRequest request
    );

    @Operation(
            summary = "카메라 이미지 파일로 식사 기록 [USER]",
            description = """
                    카메라로 촬영한 이미지 파일을 업로드하여 식사를 기록합니다.

                    - 인증된 사용자 기준으로 기록합니다.
                    - multipart/form-data 형식의 이미지 파일을 업로드합니다.
                    - Swagger에서 직접 파일 선택 후 테스트할 수 있습니다.
                    - 응답: 기록된 식사 정보
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MealErrorCode.class, code = "IMAGE_FILE_REQUIRED")
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_RECORD_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> recordMealByCamera(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId,

            @Valid @ModelAttribute MealImageUploadRequest request
    );

    @Operation(
            summary = "앨범 이미지 파일로 식사 기록 [USER]",
            description = """
                    앨범에서 선택한 이미지 파일을 업로드하여 식사를 기록합니다.

                    - 인증된 사용자 기준으로 기록합니다.
                    - multipart/form-data 형식의 이미지 파일을 업로드합니다.
                    - Swagger에서 직접 파일 선택 후 테스트할 수 있습니다.
                    - 응답: 기록된 식사 정보
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MealErrorCode.class, code = "IMAGE_FILE_REQUIRED")
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_RECORD_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> recordMealByAlbum(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId,

            @Valid @ModelAttribute MealImageUploadRequest request
    );

    @Operation(
            summary = "식단 수정 [USER]",
            description = """
                    특정 식단 기록을 수정합니다.

                    - 인증된 사용자 기준으로 수정합니다.
                    - 경로 변수: `mealId`
                    - 요청 본문: 수정할 식단 정보
                    - 응답: 수정된 식단 정보
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MealErrorCode.class, code = "MEAL_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_UPDATE_SUCCESS")
    ResponseEntity<SuccessResponse<MealResponse>> updateMeal(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId,

            @Parameter(description = "수정할 식단 ID", required = true, example = "1")
            @PathVariable("mealId") Long mealId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 식단 정보",
                    required = true
            )
            @Valid @RequestBody MealUpdateRequest request
    );

    @Operation(
            summary = "식단 삭제 [USER]",
            description = """
                    특정 식단 기록을 삭제합니다.

                    - 인증된 사용자 기준으로 삭제합니다.
                    - 경로 변수: `mealId`
                    - 응답: 없음
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MealErrorCode.class, code = "MEAL_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "MEAL_DELETE_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> deleteMeal(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId,

            @Parameter(description = "삭제할 식단 ID", required = true, example = "1")
            @PathVariable("mealId") Long mealId
    );

    @Operation(summary = "음식명 검색", description = "음식명으로 영양정보 검색")
    @GetMapping("/foods/search")
    ResponseEntity<SuccessResponse<List<FoodNutritionSearchResult>>> searchFood(
            @Parameter(description = "검색할 음식명", example = "삼각김밥")
            @RequestParam String keyword
    );
}
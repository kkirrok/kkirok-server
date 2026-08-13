package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealImageUploadRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealRecordConfirmRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.dto.response.MealScanResponse;
import com.kkirok.server.domain.meal.application.dto.response.RecommendationResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayNutritionSummaryResponse;
import com.kkirok.server.domain.meal.application.dto.response.YesterdayPickResponse;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.domain.meal.exception.MealErrorCode;
import com.kkirok.server.domain.meal.exception.MealSuccessCode;
import com.kkirok.server.domain.terms.exception.TermsErrorCode;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(
        name = "Meal API",
        description = "식사 기록 관련 API"
)
public interface MealApi {

    @Operation(
            summary = "오늘 식사 기록 조회 [USER]",
            description = """
                현재 로그인한 사용자의 오늘 식사 기록 목록을 조회합니다.

                - 인증된 사용자 기준으로 조회합니다.
                - 응답: 오늘 식사 기록 목록
                """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "MEAL_GET_SUCCESS"
    )
    ResponseEntity<SuccessResponse<List<MealResponse>>> getMeals(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "추천 운동, 식단 조회 [USER]",
            description = """
                현재 로그인한 사용자에게 식단 기반 추천 정보를 제공합니다.

                - 인증된 사용자 기준으로 조회합니다.
                - 응답: 추천 운동 및 식단 정보
                """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "MEAL_RECOMMENDATION_GET_SUCCESS"
    )
    ResponseEntity<SuccessResponse<RecommendationResponse>>
    getRecommendation(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "오늘의 영양성분 총합 조회 [USER]",
            description = """
                오늘 하루 동안 기록된 식단을 기준으로 영양성분 총합을 조회합니다.

                - 인증된 사용자 기준으로 조회합니다.
                - meal_date가 오늘 날짜인 식단들을 기준으로 계산합니다.
                - 칼로리, 탄수화물, 단백질, 지방, 당, 나트륨 총합을 반환합니다.
                """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "TODAY_NUTRITION_SUMMARY_GET_SUCCESS"
    )
    ResponseEntity<SuccessResponse<TodayNutritionSummaryResponse>>
    getTodayNutritionSummary(
            @Parameter(
                    description = "현재 로그인한 회원 ID",
                    hidden = true
            )
            Long memberId
    );

    @Operation(
            summary = "직접 입력으로 식단 기록 [USER]",
            description = """
                사용자가 음식 정보를 직접 입력하여 식단을 기록합니다.

                - 인증된 사용자 기준으로 기록합니다.
                - 응답: 기록된 식단 정보
                """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "MEAL_RECORD_SUCCESS"
    )
    ResponseEntity<SuccessResponse<MealResponse>>
    recordMealManually(
            @CurrentMember Long memberId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "직접 입력할 식단 정보",
                    required = true
            )
            @Valid
            @RequestBody
            MealCreateRequest request
    );

    @Operation(
            summary = "음식 이미지 스캔/분석 [USER]",
            description = """
                카메라/앨범 이미지를 업로드하여 AI로 음식을 분석합니다.

                - 이 단계에서는 식단이 DB에 저장되지 않습니다.
                - 응답으로 받은 값(음식명/칼로리/영양성분/imageKey)을 사용자가 확인·수정한 뒤
                  `/v1/meals/scan/confirm`으로 최종 저장을 요청해야 합니다.
                - multipart/form-data 형식의 이미지 파일을 업로드합니다.
                """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(
                    codeType = MealErrorCode.class,
                    code = "IMAGE_FILE_REQUIRED"
            ),
            @ApiErrorCodeExample(
                    codeType = MealErrorCode.class,
                    code = "UNRECOGNIZABLE_MEAL_IMAGE"
            ),
            @ApiErrorCodeExample(
                    codeType = MealErrorCode.class,
                    code = "NOT_FOOD_IMAGE"
            ),
            @ApiErrorCodeExample(
                    codeType = MealErrorCode.class,
                    code = "MEAL_ANALYSIS_FAILED"
            ),
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "MEAL_SCAN_SUCCESS"
    )
    ResponseEntity<SuccessResponse<MealScanResponse>>
    scanMeal(
            @CurrentMember Long memberId,

            @Valid
            @ModelAttribute
            MealImageUploadRequest request,

            @Parameter(
                    description = "스캔 방식",
                    example = "CAMERA"
            )
            @RequestParam
            ScanType scanType
    );

    @Operation(
            summary = "스캔 결과 확인 후 최종 식사 기록 [USER]",
            description = """
                `/v1/meals/scan` 응답을 사용자가 확인·수정한 값으로 최종 식단을 저장합니다.

                - `imageKey`는 스캔 단계에서 발급받은 값을 그대로 전달합니다.
                - 이미지를 다시 업로드하지 않아도 됩니다.
                - 응답: 저장된 식사 정보
                """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "MEAL_RECORD_SUCCESS"
    )
    ResponseEntity<SuccessResponse<MealResponse>>
    confirmMeal(
            @CurrentMember Long memberId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "확인/수정된 최종 식단 정보",
                    required = true
            )
            @Valid
            @RequestBody
            MealRecordConfirmRequest request
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
            @ApiErrorCodeExample(
                    codeType = MealErrorCode.class,
                    code = "MEAL_NOT_FOUND"
            ),
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "MEAL_UPDATE_SUCCESS"
    )
    ResponseEntity<SuccessResponse<MealResponse>>
    updateMeal(
            @CurrentMember Long memberId,

            @Parameter(
                    description = "수정할 식단 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable("mealId")
            Long mealId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 식단 정보",
                    required = true
            )
            @Valid
            @RequestBody
            MealUpdateRequest request
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
            @ApiErrorCodeExample(
                    codeType = MealErrorCode.class,
                    code = "MEAL_NOT_FOUND"
            ),
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "MEAL_DELETE_SUCCESS"
    )
    ResponseEntity<SuccessResponse<Void>>
    deleteMeal(
            @CurrentMember Long memberId,

            @Parameter(
                    description = "삭제할 식단 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable("mealId")
            Long mealId
    );

    @Operation(
            summary = "음식명 검색",
            description = "음식명으로 영양정보 검색"
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    ResponseEntity<SuccessResponse<List<FoodNutritionSearchResult>>>
    searchFood(
            @Parameter(
                    description = "검색할 음식명",
                    example = "삼각김밥"
            )
            @RequestParam
            String keyword
    );

    @Operation(
            summary = "어제 이 시간대 같은 유형 끼록이 픽 조회 [USER]",
            description = """
                같은 MealStyle을 가진 다른 유저들이 어제 현재 시간대에 기록한 식사를
                최대 4개 반환합니다.

                - 현재 시각(KST) 기준으로 시간대 슬롯을 결정합니다.
                - 06:00~10:00: 아침(BREAKFAST)
                - 10:00~15:00: 점심(LUNCH)
                - 15:00~20:00: 저녁(DINNER)
                - 그 외: 간식(SNACK)
                - 유저의 MealStyle이 미설정이면 BALANCED 기준으로 조회합니다.
                - 결과가 없으면 빈 목록을 반환합니다.
                """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(
            codeType = MealSuccessCode.class,
            code = "YESTERDAY_PICKS_GET_SUCCESS"
    )
    ResponseEntity<SuccessResponse<YesterdayPickResponse>>
    getYesterdayPicks(
            @Parameter(
                    description = "현재 로그인한 회원 ID",
                    hidden = true
            )
            Long memberId
    );
}

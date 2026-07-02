package com.kkirok.server.domain.meal.application.usecase;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealRecordConfirmRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.dto.response.MealScanResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayNutritionSummaryResponse;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.domain.ScanType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MealRecordUseCase {

    List<MealRecord> getTodayRecords(Long memberId);

    /** 직접 입력 식단 기록 */
    MealResponse create(Long memberId, MealCreateRequest request);

    /** 이미지 업로드 즉시 분석 + 저장 (끼니팝 등 내부 연동용, 리뷰 단계 없음) */
    MealResponse createMealByImage(Long memberId, MultipartFile file, ScanType scanType);

    /** 1단계: 이미지 업로드 + AI 분석 (DB 저장 X, 리뷰 화면 프리필용 결과만 반환) */
    MealScanResponse scanMeal(Long memberId, MultipartFile file, ScanType scanType);

    /** 2단계: 스캔 결과를 사용자가 확인/수정한 뒤 최종 저장 */
    MealResponse confirmMealRecord(Long memberId, MealRecordConfirmRequest request);

    MealResponse updateMeal(Long memberId, Long mealId, MealUpdateRequest request);

    void deleteMeal(Long memberId, Long mealId);

    MealRecord findMealWithOwnerCheck(Long memberId, Long mealId);

    TodayNutritionSummaryResponse getTodayNutritionSummary(Long memberId);
}
/*
package com.kkirok.server.domain.meal.application.usecase;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.domain.MealRecord;

import java.time.LocalDate;
import java.util.List;

public interface MealRecordUseCase {
    // 오늘의 식단 목록 조회
    List<MealRecord> getTodayRecords(Long memberId);

    // 직접 입력 식단기록
    MealResponse create(Long memberId, MealCreateRequest request);

    // 카메라 촬영으로 식단 기록 (AI 분석)
    MealResponse createMealByCamera(Long memberId, String imageUrl);

    // 앨범 사진으로 식단 기록 (AI 분석)
    MealResponse createMealByAlbum(Long memberId, String imageUrl);

    // 식단 수정
    MealResponse updateMeal(Long memberId, Long mealId, MealUpdateRequest request);

    // 식단 삭제
    void deleteMeal(Long memberId, Long mealId);
}
*/
package com.kkirok.server.domain.meal.application.usecase;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayNutritionSummaryResponse;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.domain.ScanType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MealRecordUseCase {

    List<MealRecord> getTodayRecords(Long memberId);

    MealResponse create(Long memberId, MealCreateRequest request);

    MealResponse createMealByCamera(Long memberId, MultipartFile file);

    MealResponse createMealByAlbum(Long memberId, MultipartFile file);

    MealResponse createMealByImage(Long memberId, MultipartFile file, ScanType scanType);

    MealResponse updateMeal(Long memberId, Long mealId, MealUpdateRequest request);

    void deleteMeal(Long memberId, Long mealId);

    MealRecord findMealWithOwnerCheck(Long memberId, Long mealId);

    TodayNutritionSummaryResponse getTodayNutritionSummary(Long memberId);
}
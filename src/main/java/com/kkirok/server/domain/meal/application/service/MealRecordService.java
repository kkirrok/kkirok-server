package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.dao.MealAiAnalysisRepository;
import com.kkirok.server.domain.meal.dao.MealImageRepository;
import com.kkirok.server.domain.meal.dao.MealNutritionRepository;
import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.meal.domain.MealAiAnalysis;
import com.kkirok.server.domain.meal.domain.MealImage;
import com.kkirok.server.domain.meal.domain.MealNutrition;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.domain.meal.exception.MealErrorCode;
import com.kkirok.server.domain.meal.exception.MealException;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.external.openai.OpenAiService;
import com.kkirok.server.global.external.openai.dto.response.OpenAiFoodAnalysisResult;
import com.kkirok.server.global.external.openai.prompt.PromptType;
import com.kkirok.server.global.external.r2.application.service.PresignedUrlService;
import com.kkirok.server.global.external.r2.application.service.R2UploadService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MealRecordService implements MealRecordUseCase {

    private final MealRecordRepository mealRecordRepository;
    private final MealNutritionRepository mealNutritionRepository;
    private final MealAiAnalysisRepository mealAiAnalysisRepository;
    private final MealImageRepository mealImageRepository;
    private final MemberUseCase memberUseCase;
    private final R2UploadService r2UploadService;
    private final PresignedUrlService presignedUrlService;
    private final OpenAiService openAiService;

    @Override
    public List<MealRecord> getTodayRecords(Long memberId) {
        LocalDate today = LocalDate.now();
        return getRangeRecords(memberId, today, today.plusDays(1));
    }

    private List<MealRecord> getRangeRecords(Long memberId, LocalDate start, LocalDate end) {
        return mealRecordRepository.getSpecifiedDateMealRecords(
                memberId,
                start.atStartOfDay(),
                end.atStartOfDay()
        );
    }

    /** 직접 입력으로 식단 기록 */
    @Override
    @Transactional
    public MealResponse create(Long memberId, MealCreateRequest request) {
        Member member = memberUseCase.findMemberByMemberId(memberId);
        MealRecord meal = MealRecord.createManual(member, request);
        mealRecordRepository.save(meal);

        MealNutrition nutrition = MealNutrition.create(
                meal,
                request.kcal(),
                request.proteinG() != null ? request.proteinG().doubleValue() : null,
                request.carbohydrateG() != null ? request.carbohydrateG().doubleValue() : null,
                request.sugarG() != null ? request.sugarG().doubleValue() : null,
                request.fatG() != null ? request.fatG().doubleValue() : null,
                request.sodiumMg() != null ? request.sodiumMg().doubleValue() : null
        );
        mealNutritionRepository.save(nutrition);
        meal.assignMealNutrition(nutrition);

        return MealResponse.from(meal);
    }

    /** 앨범에서 식단 기록 **/
    @Override
    @Transactional
    public MealResponse createMealByAlbum(Long memberId, MultipartFile file) {
        return createMealByImageFile(memberId, file, ScanType.IMAGE);
    }

    /** 카메라로 식단 기록 **/
    @Override
    @Transactional
    public MealResponse createMealByCamera(Long memberId, MultipartFile file) {
        return createMealByImageFile(memberId, file, ScanType.CAMERA);
    }

    private MealResponse createMealByImageFile(
            Long memberId,
            MultipartFile file,
            ScanType scanType
    ) {
        Member member = memberUseCase.findMemberByMemberId(memberId);

        if (file == null || file.isEmpty()) {
            throw new MealException(MealErrorCode.IMAGE_FILE_REQUIRED);
        }

        MealRecord meal = MealRecord.createByAi(member, scanType);
        mealRecordRepository.save(meal);

        String imageKey = r2UploadService.upload(file);
        mealImageRepository.save(MealImage.create(meal, imageKey));

        String imageUrl = presignedUrlService.getPresignedUrl(imageKey).toString();

        OpenAiFoodAnalysisResult result = analyzeImage(imageUrl);

        return applyAnalysisResult(meal, result);
    }

    @Override
    @Transactional
    public MealResponse updateMeal(Long memberId, Long mealId, MealUpdateRequest request) {
        MealRecord meal = findMealWithOwnerCheck(memberId, mealId);
        meal.update(request);

        if (meal.getMealNutrition() != null) {
            meal.getMealNutrition().update(
                    request.kcal(),
                    request.proteinG() != null ? request.proteinG().doubleValue() : null,
                    request.carbohydrateG() != null ? request.carbohydrateG().doubleValue() : null,
                    request.sugarG() != null ? request.sugarG().doubleValue() : null,
                    request.fatG() != null ? request.fatG().doubleValue() : null,
                    request.sodiumMg() != null ? request.sodiumMg().doubleValue() : null
            );
        }
        return MealResponse.from(meal);
    }

    @Override
    @Transactional
    public void deleteMeal(Long memberId, Long mealId) {
        MealRecord meal = findMealWithOwnerCheck(memberId, mealId);
        mealRecordRepository.delete(meal);
    }

    @Override
    public MealRecord findMealWithOwnerCheck(Long memberId, Long mealId) {
        MealRecord meal = mealRecordRepository.findByIdWithAnalyses(mealId)
                .orElseThrow(() -> new MealException(MealErrorCode.MEAL_NOT_FOUND));

        if (!meal.getMember().getId().equals(memberId)) {
            throw new ForbiddenException(MealErrorCode.MEAL_FORBIDDEN);
        }
        return meal;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * OpenAI Vision으로 이미지 분석.
     * PromptType.MEAL_ANALYSIS 프롬프트를 system instruction으로,
     * 이미지 URL + 보조 텍스트를 Vision input 배열로 전달합니다.
     */
    private OpenAiFoodAnalysisResult analyzeImage(String imageUrl) {
        String userText = """
            이미지 속 음식을 분석해서 반드시 아래 JSON 형식으로만 응답해줘.

            {
              "detected_food_name": "음식명",
              "food_category": "한식/중식/일식/양식/기타 중 하나",
              "kcal": 0,
              "carbohydrate_g": 0,
              "protein_g": 0,
              "fat_g": 0,
              "sugar_g": 0,
              "sodium_mg": 0,
              "meal_summary": "음식 요약",
              "nutrition_summary": "영양 요약",
              "positive_point": "좋은 점",
              "improvement_suggestion": "개선 제안"
            }

            주의:
            - 숫자 값은 추정치라도 반드시 숫자로 넣어줘.
            - 확실하지 않으면 null이 아니라 0을 넣어줘.
            - JSON 외의 설명 문장은 절대 넣지 마.
            """;

        return openAiService.createVisionResponse(
                PromptType.MEAL_ANALYSIS,
                imageUrl,
                userText,
                OpenAiFoodAnalysisResult.class
        );
    }

    /**
     * AI 분석 결과를 엔티티에 반영하고 저장합니다.
     *
     * <ol>
     *   <li>MealAiAnalysis  — 원본 분석 결과 보존</li>
     *   <li>MealNutrition   — 영양 정보 저장</li>
     *   <li>meal.applyAiResult()       — 음식 이름 / 시간대 업데이트</li>
     *   <li>meal.assignMealNutrition() — 영양 정보 연결</li>
     * </ol>
     */
    private MealResponse applyAnalysisResult(MealRecord meal, OpenAiFoodAnalysisResult result) {
        // MealAiAnalysis 저장
        MealAiAnalysis aiAnalysis = MealAiAnalysis.builder()
                .mealRecord(meal)
                .detectedFoodName(result.foodNameOrDefault())
                .foodCategory(result.foodCategoryOrDefault())
                .nutritionSummary(result.nutritionSummaryOrDefault())
                .rawResultJson(result.rawResultJsonOrDefault())
                .build();
        mealAiAnalysisRepository.save(aiAnalysis);

        // MealNutrition 저장
        MealNutrition nutrition = MealNutrition.create(
                meal,
                result.kcalOrDefault(),
                result.proteinOrDefault(),
                result.carbohydrateOrDefault(),
                result.sugarOrDefault(),
                result.fatOrDefault(),
                result.sodiumOrDefault()
        );
        mealNutritionRepository.save(nutrition);

        // MealRecord 업데이트
        meal.applyAiResult(result.foodNameOrDefault(), result.mealTimeSlotOrDefault());
        meal.assignMealNutrition(nutrition);

        return MealResponse.from(meal);
    }
}
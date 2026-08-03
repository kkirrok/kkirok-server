package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealRecordConfirmRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.dto.response.MealScanResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayNutritionSummaryResponse;
import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.util.RecommendedNutritionCalculator;
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
import java.time.ZoneId;
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
    private final MealImageValidator mealImageValidator;

    @Override
    public List<MealRecord> getTodayRecords(Long memberId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return mealRecordRepository.getSpecifiedDateMealRecords(
                memberId,
                today
        );
    }

    /**
     * 직접 입력으로 식단 기록
     */
    @Override
    @Transactional
    public MealResponse create(
            Long memberId,
            MealCreateRequest request
    ) {
        Member member =
                memberUseCase.findMemberByMemberId(memberId);

        MealRecord meal =
                MealRecord.createManual(member, request);

        mealRecordRepository.save(meal);

        MealNutrition nutrition = MealNutrition.create(
                meal,
                request.kcal(),
                request.proteinG() != null
                        ? request.proteinG().doubleValue()
                        : null,
                request.carbohydrateG() != null
                        ? request.carbohydrateG().doubleValue()
                        : null,
                request.sugarG() != null
                        ? request.sugarG().doubleValue()
                        : null,
                request.fatG() != null
                        ? request.fatG().doubleValue()
                        : null,
                request.sodiumMg() != null
                        ? request.sodiumMg().doubleValue()
                        : null
        );

        mealNutritionRepository.save(nutrition);
        meal.assignMealNutrition(nutrition);

        return MealResponse.from(meal);
    }

    /**
     * 이미지 업로드 즉시 분석 + 저장
     * 끼니팝 등 내부 연동용으로 리뷰 단계가 없습니다.
     */
    @Override
    @Transactional
    public MealResponse createMealByImage(
            Long memberId,
            MultipartFile file,
            ScanType scanType
    ) {
        Member member =
                memberUseCase.findMemberByMemberId(memberId);

        if (file == null || file.isEmpty()) {
            throw new MealException(
                    MealErrorCode.IMAGE_FILE_REQUIRED
            );
        }

        mealImageValidator.validate(file);

        MealRecord meal =
                MealRecord.createByAi(member, scanType);

        mealRecordRepository.save(meal);

        String imageKey = r2UploadService.upload(file);

        mealImageRepository.save(
                MealImage.create(meal, imageKey)
        );

        String imageUrl =
                presignedUrlService
                        .getPresignedUrl(imageKey)
                        .toString();

        OpenAiFoodAnalysisResult result =
                analyzeImage(imageUrl);

        return applyAnalysisResult(meal, result);
    }

    /**
     * 1단계: 이미지 업로드 + AI 분석만 수행
     * DB에 MealRecord를 저장하지 않습니다.
     */
    @Override
    public MealScanResponse scanMeal(
            Long memberId,
            MultipartFile file,
            ScanType scanType
    ) {
        // 회원 존재 검증만 수행
        memberUseCase.findMemberByMemberId(memberId);

        if (file == null || file.isEmpty()) {
            throw new MealException(
                    MealErrorCode.IMAGE_FILE_REQUIRED
            );
        }

        mealImageValidator.validate(file);

        String imageKey = r2UploadService.upload(file);

        String imageUrl =
                presignedUrlService
                        .getPresignedUrl(imageKey)
                        .toString();

        OpenAiFoodAnalysisResult result =
                analyzeImage(imageUrl);

        return MealScanResponse.from(
                imageKey,
                scanType,
                result
        );
    }

    /**
     * 2단계: 스캔 결과를 사용자가 확인하거나 수정한 뒤 최종 저장
     */
    @Override
    @Transactional
    public MealResponse confirmMealRecord(
            Long memberId,
            MealRecordConfirmRequest request
    ) {
        Member member =
                memberUseCase.findMemberByMemberId(memberId);

        MealRecord meal =
                MealRecord.createFromScan(member, request);

        mealRecordRepository.save(meal);

        mealImageRepository.save(
                MealImage.create(
                        meal,
                        request.imageKey()
                )
        );

        MealNutrition nutrition = MealNutrition.create(
                meal,
                request.kcal(),
                request.proteinG() != null
                        ? request.proteinG().doubleValue()
                        : null,
                request.carbohydrateG() != null
                        ? request.carbohydrateG().doubleValue()
                        : null,
                request.sugarG() != null
                        ? request.sugarG().doubleValue()
                        : null,
                request.fatG() != null
                        ? request.fatG().doubleValue()
                        : null,
                request.sodiumMg() != null
                        ? request.sodiumMg().doubleValue()
                        : null
        );

        mealNutritionRepository.save(nutrition);
        meal.assignMealNutrition(nutrition);

        return MealResponse.from(meal);
    }

    @Override
    @Transactional
    public MealResponse updateMeal(
            Long memberId,
            Long mealId,
            MealUpdateRequest request
    ) {
        MealRecord meal =
                findMealWithOwnerCheck(memberId, mealId);

        meal.update(request);

        if (meal.getMealNutrition() != null) {
            meal.getMealNutrition().update(
                    request.kcal(),
                    request.proteinG() != null
                            ? request.proteinG().doubleValue()
                            : null,
                    request.carbohydrateG() != null
                            ? request.carbohydrateG().doubleValue()
                            : null,
                    request.sugarG() != null
                            ? request.sugarG().doubleValue()
                            : null,
                    request.fatG() != null
                            ? request.fatG().doubleValue()
                            : null,
                    request.sodiumMg() != null
                            ? request.sodiumMg().doubleValue()
                            : null
            );
        }

        return MealResponse.from(meal);
    }

    @Override
    @Transactional
    public void deleteMeal(
            Long memberId,
            Long mealId
    ) {
        MealRecord meal =
                findMealWithOwnerCheck(memberId, mealId);

        mealRecordRepository.delete(meal);
    }

    @Override
    public MealRecord findMealWithOwnerCheck(
            Long memberId,
            Long mealId
    ) {
        MealRecord meal =
                mealRecordRepository
                        .findByIdWithAnalyses(mealId)
                        .orElseThrow(
                                () -> new MealException(
                                        MealErrorCode.MEAL_NOT_FOUND
                                )
                        );

        if (!meal.getMember().getId().equals(memberId)) {
            throw new ForbiddenException(
                    MealErrorCode.MEAL_FORBIDDEN
            );
        }

        return meal;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * OpenAI Vision으로 이미지 분석.
     *
     * PromptType.MEAL_ANALYSIS 프롬프트를 system instruction으로,
     * 이미지 URL과 보조 텍스트를 Vision input 배열로 전달합니다.
     */
    private OpenAiFoodAnalysisResult analyzeImage(
            String imageUrl
    ) {
        String userText = """
            먼저 이미지에 음식이 선명하게 보이는지 판단해줘.
            검은 화면, 빈 화면, 심하게 어둡거나 밝은 사진, 음식이 없는 사진이면
            절대로 음식명을 추측하지 말고 is_food를 false로 응답해줘.

            음식이 명확하게 보이는 경우에만 is_food를 true로 하고,
            시스템 지침에 정의된 JSON 필드를 모두 채워서 JSON만 반환해줘.
            """;

        OpenAiFoodAnalysisResult result =
                openAiService.createVisionResponse(
                        PromptType.MEAL_ANALYSIS,
                        imageUrl,
                        userText,
                        OpenAiFoodAnalysisResult.class
                );

        if (result == null
                || !result.isReliableFoodDetection()) {
            throw new MealException(
                    MealErrorCode.UNRECOGNIZABLE_MEAL_IMAGE
            );
        }

        return result;
    }

    /**
     * AI 분석 결과를 엔티티에 반영하고 저장합니다.
     * createMealByImage 전용 즉시 저장 플로우입니다.
     *
     * 1. MealAiAnalysis: 원본 분석 결과 보존
     * 2. MealNutrition: 영양 정보 저장
     * 3. meal.applyAiResult(): 음식 이름 및 시간대 업데이트
     * 4. meal.assignMealNutrition(): 영양 정보 연결
     */
    private MealResponse applyAnalysisResult(
            MealRecord meal,
            OpenAiFoodAnalysisResult result
    ) {
        MealAiAnalysis aiAnalysis =
                MealAiAnalysis.builder()
                        .mealRecord(meal)
                        .detectedFoodName(
                                result.foodNameOrDefault()
                        )
                        .foodCategory(
                                result.foodCategoryOrDefault()
                        )
                        .nutritionSummary(
                                result.nutritionSummaryOrDefault()
                        )
                        .rawResultJson(
                                result.rawResultJsonOrDefault()
                        )
                        .build();

        mealAiAnalysisRepository.save(aiAnalysis);

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

        meal.applyAiResult(
                result.foodNameOrDefault(),
                result.mealTimeSlotOrDefault()
        );

        meal.assignMealNutrition(nutrition);

        return MealResponse.from(meal);
    }

    @Override
    public TodayNutritionSummaryResponse getTodayNutritionSummary(
            Long memberId
    ) {
        LocalDate today =
                LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<MealRecord> records =
                mealRecordRepository.getSpecifiedDateMealRecords(
                        memberId,
                        today
                );

        int totalKcal = 0;
        long totalCarbohydrateG = 0L;
        long totalProteinG = 0L;
        long totalFatG = 0L;
        long totalSugarG = 0L;
        long totalSodiumMg = 0L;

        for (MealRecord record : records) {
            if (record.getMealNutrition() == null) {
                continue;
            }

            totalKcal +=
                    record.getMealNutrition().getKcal() == null
                            ? 0
                            : record.getMealNutrition().getKcal();

            totalCarbohydrateG +=
                    record.getMealNutrition()
                            .getCarbohydrateG() == null
                            ? 0L
                            : record.getMealNutrition()
                            .getCarbohydrateG()
                            .longValue();

            totalProteinG +=
                    record.getMealNutrition()
                            .getProteinG() == null
                            ? 0L
                            : record.getMealNutrition()
                            .getProteinG()
                            .longValue();

            totalFatG +=
                    record.getMealNutrition()
                            .getFatG() == null
                            ? 0L
                            : record.getMealNutrition()
                            .getFatG()
                            .longValue();

            totalSugarG +=
                    record.getMealNutrition()
                            .getSugarG() == null
                            ? 0L
                            : record.getMealNutrition()
                            .getSugarG()
                            .longValue();

            totalSodiumMg +=
                    record.getMealNutrition()
                            .getSodiumMg() == null
                            ? 0L
                            : record.getMealNutrition()
                            .getSodiumMg()
                            .longValue();
        }

        Member member =
                memberUseCase.findWithOnboarding(memberId);

        RecommendedNutritionCalculator.NutritionRecommendation
                recommendation =
                RecommendedNutritionCalculator.calculate(
                        member.getGender(),
                        member.getBirthday(),
                        member.getOnboarding() != null
                                ? member.getOnboarding().getPurpose()
                                : null,
                        today
                );

        return new TodayNutritionSummaryResponse(
                totalKcal,
                totalCarbohydrateG,
                totalProteinG,
                totalFatG,
                totalSugarG,
                totalSodiumMg,
                recommendation.kcal(),
                recommendation.carbohydrateG(),
                recommendation.proteinG(),
                recommendation.fatG(),
                recommendation.sugarG(),
                recommendation.sodiumMg()
        );
    }
}
package com.kkirok.server.domain.meal.util;

import com.kkirok.server.domain.member.domain.Gender;
import com.kkirok.server.domain.member.domain.OnboardingPurpose;

import java.time.LocalDate;
import java.time.Period;

/**
 * 성별 / 나이 / 목적에 따라 하루 권장 영양소를 계산합니다.
 *
 * <p>계산 방식</p>
 * <ol>
 *   <li>Harris-Benedict BMR 공식으로 기초 대사량 산출 (신장·체중 미보유 → 성별·나이 평균값 사용)</li>
 *   <li>좌식 생활 기준 활동계수(×1.375) 적용하여 TDEE 산출</li>
 *   <li>목적별 칼로리 보정: 감량 -300 kcal / 증량 +300 kcal / 유지·습관 그대로</li>
 *   <li>탄수화물 50% / 단백질 20% / 지방 30% 비율로 분배</li>
 * </ol>
 */
public final class RecommendedNutritionCalculator {

    // 활동계수 (좌식 기준)
    private static final double ACTIVITY_FACTOR = 1.375;

    // 목적별 칼로리 조정값 (kcal)
    private static final int LOSE_WEIGHT_ADJUSTMENT  = -300;
    private static final int GAIN_WEIGHT_ADJUSTMENT  =  300;

    // 영양소 칼로리 환산 (1g당 kcal)
    private static final double KCAL_PER_G_CARB    = 4.0;
    private static final double KCAL_PER_G_PROTEIN = 4.0;
    private static final double KCAL_PER_G_FAT     = 9.0;

    // 칼로리 배분 비율
    private static final double RATIO_CARB    = 0.50;
    private static final double RATIO_PROTEIN = 0.20;
    private static final double RATIO_FAT     = 0.30;

    // 성별별 평균 신장·체중 (신체 정보 미보유 시 사용)
    private static final double DEFAULT_HEIGHT_MALE   = 173.0; // cm
    private static final double DEFAULT_WEIGHT_MALE   =  73.0; // kg
    private static final double DEFAULT_HEIGHT_FEMALE = 160.0;
    private static final double DEFAULT_WEIGHT_FEMALE =  57.0;

    private RecommendedNutritionCalculator() {}

    public record NutritionRecommendation(
            int kcal,
            int carbohydrateG,
            int proteinG,
            int fatG
    ) {}

    /**
     * 권장 영양소를 계산합니다.
     *
     * @param gender   성별 (null 이면 FEMALE 기준으로 fallback)
     * @param birthday 생년월일 (null 이면 25세 기준으로 fallback)
     * @param purpose  온보딩 목적 (null 이면 MAINTAIN 기준으로 fallback)
     * @param date     기준 날짜 (나이 계산용)
     */
    public static NutritionRecommendation calculate(
            Gender gender,
            LocalDate birthday,
            OnboardingPurpose purpose,
            LocalDate date
    ) {
        int age = resolveAge(birthday, date);
        boolean isMale = (gender == null || gender == Gender.FEMALE);

        double bmr = calculateBmr(isMale, age); // BMR (Basal Metabolic Rate) - 기초대사량
        double tdee = bmr * ACTIVITY_FACTOR;    // TDEE (Total Daily Energy Expenditure) - 하루 총 에너지 소비량
        double targetKcal = applyPurposeAdjustment(tdee, purpose);

        int kcal           = (int) Math.round(targetKcal);
        int carbohydrateG  = (int) Math.round(targetKcal * RATIO_CARB    / KCAL_PER_G_CARB);
        int proteinG       = (int) Math.round(targetKcal * RATIO_PROTEIN / KCAL_PER_G_PROTEIN);
        int fatG           = (int) Math.round(targetKcal * RATIO_FAT     / KCAL_PER_G_FAT);

        return new NutritionRecommendation(kcal, carbohydrateG, proteinG, fatG);
    }

    // ────────────────────────────────────────────
    // private helpers
    // ────────────────────────────────────────────

    private static int resolveAge(LocalDate birthday, LocalDate referenceDate) {
        if (birthday == null) return 25;
        LocalDate ref = (referenceDate != null) ? referenceDate : LocalDate.now();
        return Period.between(birthday, ref).getYears();
    }

    /**
     * Harris-Benedict 개정판 BMR
     * 남성: 88.362 + (13.397 × 체중kg) + (4.799 × 신장cm) − (5.677 × 나이)
     * 여성: 447.593 + (9.247 × 체중kg) + (3.098 × 신장cm) − (4.330 × 나이)
     */
    private static double calculateBmr(boolean isMale, int age) {
        if (isMale) {
            return 88.362
                    + (13.397 * DEFAULT_WEIGHT_MALE)
                    + (4.799  * DEFAULT_HEIGHT_MALE)
                    - (5.677  * age);
        } else {
            return 447.593
                    + (9.247  * DEFAULT_WEIGHT_FEMALE)
                    + (3.098  * DEFAULT_HEIGHT_FEMALE)
                    - (4.330  * age);
        }
    }

    private static double applyPurposeAdjustment(double tdee, OnboardingPurpose purpose) {
        if (purpose == null) return tdee;
        return switch (purpose) {
            case LOSE_WEIGHT -> tdee + LOSE_WEIGHT_ADJUSTMENT;
            case GAIN_WEIGHT -> tdee + GAIN_WEIGHT_ADJUSTMENT;
            default          -> tdee; // MAINTAIN, HABIT
        };
    }
}

package com.kkirok.server.domain.home.application.service;

import com.kkirok.server.domain.home.application.dto.response.HomeResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayNutritionSummaryResponse;
import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.policy.MealReminderPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 홈화면에 필요한 도메인 정보를 가져와 조합하여 반환합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeService {

    private static final double UNDER_KCAL_RATIO = 0.7;
    private static final double OVER_KCAL_RATIO = 1.1;

    private final MemberUseCase memberUseCase;
    private final MealRecordUseCase mealRecordUseCase;
    private final List<MealReminderPolicy> mealReminderPolicies;

    public HomeResponse getHomeInfo(Long memberId) {

        Member member = memberUseCase.findMemberByMemberId(memberId);

        // 멤버정보
        MealStyle mealStyle = member.getMealStyle();
        HomeResponse.HomeMemberInfo homeMemberInfo = new HomeResponse.HomeMemberInfo(mealStyle, mealStyle.getLabel(), member.getNickname());

        // 끼록 리마인더
        MealReminderPolicy triggeredPolicy = mealReminderPolicies.stream() // 내가 타겟이 되는 리마인더 있으면 그걸 이용
                .filter(policy -> policy.isImTarget(LocalDateTime.now(), memberId))
                .findFirst()
                .orElse(null);
        HomeResponse.HomeReminder homeReminder = triggeredPolicy != null
                ? new HomeResponse.HomeReminder(true, triggeredPolicy.title(), triggeredPolicy.body())
                : new HomeResponse.HomeReminder(false, null, null);

        // 오늘의 영양 요약
        TodayNutritionSummaryResponse nutrition = mealRecordUseCase.getTodayNutritionSummary(memberId);

        // 오늘 섭취량 기반 피드백
        HomeResponse.HomeFeedback feedback = buildFeedback(nutrition);

        // 조립 후 반환
        return new HomeResponse(
                homeMemberInfo,
                homeReminder,
                nutrition,
                feedback
        );
    }

    private HomeResponse.HomeFeedback buildFeedback(TodayNutritionSummaryResponse nutrition) {
        if (nutrition.totalKcal() == null || nutrition.totalKcal() == 0) {
            return new HomeResponse.HomeFeedback(
                    HomeResponse.HomeKcalStatus.NO_RECORD,
                    "아직 기록한 식사가 없어요",
                    "오늘의 첫 끼니를 끼록해보세요"
            );
        }

        Integer recommendedKcal = nutrition.recommendedKcal();
        if (recommendedKcal == null || recommendedKcal == 0) {
            return new HomeResponse.HomeFeedback(
                    HomeResponse.HomeKcalStatus.GOOD,
                    "오늘도 끼록하고 있어요",
                    "꾸준히 기록하면 더 정확한 피드백을 받을 수 있어요"
            );
        }

        double ratio = (double) nutrition.totalKcal() / recommendedKcal;
        if (ratio < UNDER_KCAL_RATIO) {
            return new HomeResponse.HomeFeedback(
                    HomeResponse.HomeKcalStatus.UNDER,
                    "오늘 조금 더 채워볼까요?",
                    "권장 칼로리보다 적게 섭취했어요"
            );
        }
        if (ratio > OVER_KCAL_RATIO) {
            return new HomeResponse.HomeFeedback(
                    HomeResponse.HomeKcalStatus.OVER,
                    "오늘 조금 과식했어요",
                    "권장 칼로리보다 많이 섭취했어요"
            );
        }
        return new HomeResponse.HomeFeedback(
                HomeResponse.HomeKcalStatus.GOOD,
                "오늘도 잘 챙겨 먹었어요",
                "권장 칼로리에 맞게 균형있게 섭취했어요"
        );
    }
}

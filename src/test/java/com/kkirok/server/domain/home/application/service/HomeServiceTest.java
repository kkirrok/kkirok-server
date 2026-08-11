package com.kkirok.server.domain.home.application.service;

import com.kkirok.server.domain.home.application.dto.response.HomeResponse;
import com.kkirok.server.domain.meal.application.dto.response.TodayNutritionSummaryResponse;
import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.policy.MealReminderPolicy;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    private MemberUseCase memberUseCase;

    @Mock
    private MealRecordUseCase mealRecordUseCase;

    @Mock
    private MealReminderPolicy triggeredPolicy;

    @Mock
    private MealReminderPolicy notTriggeredPolicy;

    @Test
    @DisplayName("리마인더 대상이면 정책의 title/body로 리마인더를 채운다")
    void shouldFillReminderWhenPolicyMatches() {
        HomeService homeService = new HomeService(memberUseCase, mealRecordUseCase, List.of(triggeredPolicy, notTriggeredPolicy));
        given(memberUseCase.findMemberByMemberId(MEMBER_ID)).willReturn(createMember(MealStyle.BALANCED));
        given(triggeredPolicy.isImTarget(any(LocalDateTime.class), eq(MEMBER_ID))).willReturn(true);
        given(triggeredPolicy.title()).willReturn("지금 끼록할 시간이에요");
        given(triggeredPolicy.body()).willReturn("오늘의 식사를 찍고 간단하게 남겨보세요");
        given(mealRecordUseCase.getTodayNutritionSummary(MEMBER_ID)).willReturn(nutrition(1500, 2000));

        HomeResponse response = homeService.getHomeInfo(MEMBER_ID);

        assertThat(response.reminder().isTimeToKkirok()).isTrue();
        assertThat(response.reminder().title()).isEqualTo("지금 끼록할 시간이에요");
        assertThat(response.reminder().description()).isEqualTo("오늘의 식사를 찍고 간단하게 남겨보세요");
    }

    @Test
    @DisplayName("리마인더 대상이 아니면 isTimeToKkirok=false, title/description은 null이다")
    void shouldReturnNoReminderWhenNoPolicyMatches() {
        HomeService homeService = new HomeService(memberUseCase, mealRecordUseCase, List.of(notTriggeredPolicy));
        given(memberUseCase.findMemberByMemberId(MEMBER_ID)).willReturn(createMember(MealStyle.BALANCED));
        given(notTriggeredPolicy.isImTarget(any(LocalDateTime.class), eq(MEMBER_ID))).willReturn(false);
        given(mealRecordUseCase.getTodayNutritionSummary(MEMBER_ID)).willReturn(nutrition(1500, 2000));

        HomeResponse response = homeService.getHomeInfo(MEMBER_ID);

        assertThat(response.reminder().isTimeToKkirok()).isFalse();
        assertThat(response.reminder().title()).isNull();
        assertThat(response.reminder().description()).isNull();
    }

    @Test
    @DisplayName("오늘 기록한 끼니가 없으면 NO_RECORD 피드백을 반환한다")
    void shouldReturnNoRecordFeedbackWhenNoMealToday() {
        HomeService homeService = new HomeService(memberUseCase, mealRecordUseCase, List.of(notTriggeredPolicy));
        given(memberUseCase.findMemberByMemberId(MEMBER_ID)).willReturn(createMember(MealStyle.BALANCED));
        given(notTriggeredPolicy.isImTarget(any(LocalDateTime.class), eq(MEMBER_ID))).willReturn(false);
        given(mealRecordUseCase.getTodayNutritionSummary(MEMBER_ID)).willReturn(nutrition(0, 2000));

        HomeResponse response = homeService.getHomeInfo(MEMBER_ID);

        assertThat(response.feedback().kcalStatus()).isEqualTo(HomeResponse.HomeKcalStatus.NO_RECORD);
    }

    @Test
    @DisplayName("권장 칼로리의 70% 미만이면 UNDER 피드백을 반환한다")
    void shouldReturnUnderFeedbackWhenBelowThreshold() {
        HomeService homeService = new HomeService(memberUseCase, mealRecordUseCase, List.of(notTriggeredPolicy));
        given(memberUseCase.findMemberByMemberId(MEMBER_ID)).willReturn(createMember(MealStyle.BALANCED));
        given(notTriggeredPolicy.isImTarget(any(LocalDateTime.class), eq(MEMBER_ID))).willReturn(false);
        given(mealRecordUseCase.getTodayNutritionSummary(MEMBER_ID)).willReturn(nutrition(1000, 2000));

        HomeResponse response = homeService.getHomeInfo(MEMBER_ID);

        assertThat(response.feedback().kcalStatus()).isEqualTo(HomeResponse.HomeKcalStatus.UNDER);
    }

    @Test
    @DisplayName("권장 칼로리의 70~110% 사이면 GOOD 피드백을 반환한다")
    void shouldReturnGoodFeedbackWhenWithinRange() {
        HomeService homeService = new HomeService(memberUseCase, mealRecordUseCase, List.of(notTriggeredPolicy));
        given(memberUseCase.findMemberByMemberId(MEMBER_ID)).willReturn(createMember(MealStyle.BALANCED));
        given(notTriggeredPolicy.isImTarget(any(LocalDateTime.class), eq(MEMBER_ID))).willReturn(false);
        given(mealRecordUseCase.getTodayNutritionSummary(MEMBER_ID)).willReturn(nutrition(1900, 2000));

        HomeResponse response = homeService.getHomeInfo(MEMBER_ID);

        assertThat(response.feedback().kcalStatus()).isEqualTo(HomeResponse.HomeKcalStatus.GOOD);
    }

    @Test
    @DisplayName("권장 칼로리의 110% 초과면 OVER 피드백을 반환한다")
    void shouldReturnOverFeedbackWhenAboveThreshold() {
        HomeService homeService = new HomeService(memberUseCase, mealRecordUseCase, List.of(notTriggeredPolicy));
        given(memberUseCase.findMemberByMemberId(MEMBER_ID)).willReturn(createMember(MealStyle.BALANCED));
        given(notTriggeredPolicy.isImTarget(any(LocalDateTime.class), eq(MEMBER_ID))).willReturn(false);
        given(mealRecordUseCase.getTodayNutritionSummary(MEMBER_ID)).willReturn(nutrition(2500, 2000));

        HomeResponse response = homeService.getHomeInfo(MEMBER_ID);

        assertThat(response.feedback().kcalStatus()).isEqualTo(HomeResponse.HomeKcalStatus.OVER);
    }

    private Member createMember(MealStyle mealStyle) {
        Member member = MemberFixture.createLocalMember("끼록이", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "mealStyle", mealStyle);
        return member;
    }

    private TodayNutritionSummaryResponse nutrition(int totalKcal, int recommendedKcal) {
        return new TodayNutritionSummaryResponse(
                totalKcal, 0L, 0L, 0L, 0L, 0L,
                recommendedKcal, 0, 0, 0, 0, 0
        );
    }
}

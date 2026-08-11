package com.kkirok.server.domain.home.application.dto.response;

import com.kkirok.server.domain.meal.application.dto.response.TodayNutritionSummaryResponse;
import com.kkirok.server.domain.member.domain.MealStyle;

public record HomeResponse(

        HomeMemberInfo memberInfo,
        HomeReminder reminder,
        TodayNutritionSummaryResponse nutrition,
        HomeFeedback feedback

) {

    public record HomeMemberInfo(
            MealStyle mealStyle,
            String mealStyleLabel,
            String nickname
    ){}

    public record HomeReminder(
            Boolean isTimeToKkirok,
            String title,
            String description
    ){}

    public enum HomeKcalStatus {
        OVER, GOOD, UNDER, NO_RECORD
    }

    public record HomeFeedback(
            HomeKcalStatus kcalStatus,
            String title,
            String comment
    ){}

}

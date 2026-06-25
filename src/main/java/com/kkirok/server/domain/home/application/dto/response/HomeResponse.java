package com.kkirok.server.domain.home.application.dto.response;

import com.kkirok.server.domain.member.domain.MealStyle;

public record HomeResponse(

        HomeMemberInfo memberInfo,
        HomeReminder reminder,
        HomeNutrition nutrition,
        HomeFeedback feedback

) {

    public record HomeMemberInfo(
            MealStyle mealStyle,
            String mealStyleLabel,
            String nickname
    ){}

    public record HomeReminder(
            Boolean isTimeToKkirok,
            String description
    )
    {}

    public record HomeNutrition(){

    }

    public record HomeFeedback(){


    }

    public record HomeFeedbackCard(){ // 홈화면 피드백 하단 카드섹션 부분

    }

    public record HomeFeedbackBottom(){ // 홈화면 최하단 부분 TODO: 이 클래스 이름은 적절하게 다시 정의 부탁드립니다

    }



}

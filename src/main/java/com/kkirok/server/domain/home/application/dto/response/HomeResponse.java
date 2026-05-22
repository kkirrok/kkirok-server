package com.kkirok.server.domain.home.application.dto.response;

public record HomeResponse(

        HomeCharacter character,
        HomeReminder reminder,
        HomeNutrition nutrition,
        HomeFeedback feedback

) {

    public record HomeCharacter(){

    }

    public record HomeReminder(){

    }

    public record HomeNutrition(){

    }

    public record HomeFeedback(){


    }

    public record HomeFeedbackCard(){ // 홈화면 피드백 하단 카드섹션 부분

    }

    public record HomeFeedbackBottom(){ // 홈화면 최하단 부분 TODO: 이 클래스 이름은 적절하게 다시 정의 부탁드립니다

    }



}

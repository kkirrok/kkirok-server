package com.kkirok.server.domain.member.application.dto.response;

import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.Member;

public record MyPageResponse(

        Long memberId,
        String name,
        String profileImage,
        MealStyle mealStyle,
        String mealStyleLabel,
        Integer recommendedKcal

) {

    public static MyPageResponse of(Member member, Integer recommendedKcal) {
        return new MyPageResponse(
                member.getId(),
                member.getName(),
                member.getProfileImage(),
                member.getMealStyle(),
                member.getMealStyle().getLabel(),
                recommendedKcal
        );
    }

}

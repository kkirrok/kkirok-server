package com.kkirok.server.domain.meal.application.dto.response;

import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import com.kkirok.server.domain.member.domain.MealStyle;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "어제 이 시간대 같은 유형 끼록이들의 픽")
public record YesterdayPickResponse(

        @Schema(description = "현재 유저의 식사 유형", example = "BALANCED")
        MealStyle mealStyle,

        @Schema(description = "현재 시간대 슬롯", example = "LUNCH")
        MealTimeSlot timeSlot,

        @Schema(description = "어제 같은 시간대 픽 목록")
        List<PickItem> picks

) {

    @Schema(description = "식사 픽 아이템")
    public record PickItem(

            @Schema(description = "식사 기록 ID", example = "42")
            Long mealId,

            @Schema(description = "음식 이름", example = "닭가슴살 샐러드")
            String foodName,

            @Schema(description = "칼로리", example = "350")
            Integer kcal,

            @Schema(description = "이미지 URL (없으면 null)", example = "https://cdn.example.com/meal/42.jpg")
            String imageUrl

    ) {}
}
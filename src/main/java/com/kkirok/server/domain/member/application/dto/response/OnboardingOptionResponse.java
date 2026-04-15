package com.kkirok.server.domain.member.application.dto.response;

import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.domain.member.domain.OnboardingPurpose;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "온보딩 선택지")
public record OnboardingOptionResponse(

        @Schema(example = "LOSE_WEIGHT", description = "enum 상수")
        String value,

        @Schema(example = "감량", description = "선택지 라벨")
        String label

) {
    public static OnboardingOptionResponse from(final OnboardingPurpose purpose) {
        return new OnboardingOptionResponse(purpose.name(), purpose.getLabel());
    }

    public static OnboardingOptionResponse from(final OnboardingHabit habit) {
        return new OnboardingOptionResponse(habit.name(), habit.getLabel());
    }
}

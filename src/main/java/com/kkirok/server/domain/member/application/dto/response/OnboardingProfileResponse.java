package com.kkirok.server.domain.member.application.dto.response;

import com.kkirok.server.domain.member.domain.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;

public record OnboardingProfileResponse(

        @Schema(example = "끼록이", description = "현재 닉네임")
        String nickname,

        @Schema(example = "MALE", description = "현재 성별", nullable = true)
        Gender gender,

        @Schema(example = "550e8400-e29b-41d4-a716-446655440000-profile.png", description = "프로필 이미지 저장 key", nullable = true)
        String profileImage,

        @ArraySchema(
                arraySchema = @Schema(description = "온보딩 목표 선택지 목록"),
                schema = @Schema(implementation = OnboardingLabelInfo.class)
        )
        List<OnboardingLabelInfo> purposes,

        @ArraySchema(
                arraySchema = @Schema(description = "식습관 유형 선택지 목록"),
                schema = @Schema(implementation = OnboardingLabelInfo.class)
        )
        List<OnboardingLabelInfo> habits

) {

    public static OnboardingProfileResponse of(final Member member) {

        Onboarding onboarding = member.getOnboarding();
        OnboardingPurpose selectedPurpose = onboarding == null ? null : onboarding.getPurpose();
        Set<OnboardingHabit> selectedHabits = onboarding == null
                ? Set.of()
                : Set.copyOf(onboarding.getHabits());

        return new OnboardingProfileResponse(
                member.getNickname(),
                member.getGender(),
                member.getProfileImage(),
                createPurposeInfos(selectedPurpose),
                createHabitInfos(selectedHabits)
        );
    }

    private static List<OnboardingLabelInfo> createPurposeInfos(final OnboardingPurpose selectedPurpose) {
        return List.of(OnboardingPurpose.values()).stream()
                .map(purpose -> OnboardingLabelInfo.of(purpose, purpose == selectedPurpose))
                .toList();
    }

    private static List<OnboardingLabelInfo> createHabitInfos(final Set<OnboardingHabit> selectedHabits) {
        return List.of(OnboardingHabit.values()).stream()
                .map(habit -> OnboardingLabelInfo.of(habit, selectedHabits.contains(habit)))
                .toList();
    }

    @Schema(name = "OnboardingLabelInfo", description = "온보딩 선택지와 선택 여부")
    public record OnboardingLabelInfo(
            @Schema(example = "감량", description = "선택지 라벨")
            String label,

            @Schema(example = "true", description = "현재 선택 여부")
            Boolean isSelected
    ) {
        public static OnboardingLabelInfo of(OnboardingPurpose purpose, boolean isSelected) {
            return new OnboardingLabelInfo(purpose.getLabel(), isSelected);
        }

        public static OnboardingLabelInfo of(OnboardingHabit habit, boolean isSelected) {
            return new OnboardingLabelInfo(habit.getLabel(), isSelected);
        }
    }
}

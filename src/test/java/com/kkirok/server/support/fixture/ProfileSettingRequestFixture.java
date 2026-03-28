package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.member.domain.Gender;
import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.domain.member.domain.OnboardingPurpose;

import java.time.LocalDate;
import java.util.List;

public final class ProfileSettingRequestFixture {

    private ProfileSettingRequestFixture() {
    }

    public static ProfileSettingRequest create() {
        return new ProfileSettingRequest(
                "김준용",
                LocalDate.of(2002, 4, 13),
                "01012345678",
                "끼록이",
                Gender.MALE,
                OnboardingPurpose.HABIT,
                List.of(OnboardingHabit.MEAT, OnboardingHabit.SNACK)
        );
    }

    public static ProfileSettingRequest create(List<OnboardingHabit> habits) {
        return new ProfileSettingRequest(
                "김준용",
                LocalDate.of(2002, 4, 13),
                "01012345678",
                "끼록이",
                Gender.MALE,
                OnboardingPurpose.HABIT,
                habits
        );
    }

    public static ProfileSettingRequest create(OnboardingPurpose purpose, List<OnboardingHabit> habits) {
        return new ProfileSettingRequest(
                "김준용",
                LocalDate.of(2002, 4, 13),
                "01012345678",
                "끼록이",
                Gender.MALE,
                purpose,
                habits
        );
    }
}

package com.kkirok.server.domain.member.domain;

import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.support.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemberOnboardingTest {

    @Test
    @DisplayName("온보딩 정보를 처음 적용하면 회원과 온보딩이 양방향으로 연결된다")
    void shouldCreateOnboardingAndBindBothSides() {
        Member member = Member.createLocal("kkirok@test.com", UserFixture.create());
        ProfileSettingRequest request = createRequest(
                "첫닉네임",
                OnboardingPurpose.HABIT,
                List.of(OnboardingHabit.MEAT, OnboardingHabit.SNACK)
        );

        member.updateOnboarding(request, "profile-key");

        assertThat(member.isOnboardingCompleted()).isTrue();
        assertThat(member.getNickname()).isEqualTo("첫닉네임");
        assertThat(member.getOnboarding()).isNotNull();
        assertThat(member.getOnboarding().getMember()).isSameAs(member);
        assertThat(member.getOnboarding().getPurpose()).isEqualTo(OnboardingPurpose.HABIT);
        assertThat(member.getOnboarding().getHabits()).containsExactly(
                OnboardingHabit.MEAT,
                OnboardingHabit.SNACK
        );
    }

    @Test
    @DisplayName("기존 온보딩이 있으면 새로 만들지 않고 내용을 갱신한다")
    void shouldUpdateExistingOnboarding() {
        Member member = Member.createLocal("kkirok@test.com", UserFixture.create());
        member.updateOnboarding(
                createRequest("첫닉네임", OnboardingPurpose.HABIT, List.of(OnboardingHabit.MEAT)),
                "profile-key-1"
        );

        Onboarding onboarding = member.getOnboarding();

        member.updateOnboarding(
                createRequest("둘째닉네임", OnboardingPurpose.LOSE_WEIGHT, List.of(OnboardingHabit.DIET, OnboardingHabit.REGULAR_MEAL)),
                "profile-key-2"
        );

        assertThat(member.getOnboarding()).isSameAs(onboarding);
        assertThat(member.getOnboarding().getMember()).isSameAs(member);
        assertThat(member.getOnboarding().getPurpose()).isEqualTo(OnboardingPurpose.LOSE_WEIGHT);
        assertThat(member.getOnboarding().getHabits()).containsExactly(
                OnboardingHabit.DIET,
                OnboardingHabit.REGULAR_MEAL
        );
        assertThat(member.getNickname()).isEqualTo("둘째닉네임");
        assertThat(member.getProfileImage()).isEqualTo("profile-key-2");
    }

    private ProfileSettingRequest createRequest(
            final String nickname,
            final OnboardingPurpose purpose,
            final List<OnboardingHabit> habits
    ) {
        return new ProfileSettingRequest(
                "김준용",
                LocalDate.of(2002, 4, 13),
                "01012345678",
                nickname,
                Gender.MALE,
                purpose,
                habits
        );
    }
}

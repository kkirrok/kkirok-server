package com.kkirok.server.domain.member.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.member.util.OnboardingHabitListConverter;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "onboarding")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Onboarding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 30)
    private OnboardingPurpose purpose;

    @Convert(converter = OnboardingHabitListConverter.class)
    @Column(name = "habits", length = 255)
    private List<OnboardingHabit> habits;

    @Builder
    private Onboarding(final Member member, final OnboardingPurpose purpose, final List<OnboardingHabit> habits) {
        updatePurposeAndHabits(purpose, habits);
        changeMember(member);
    }

    public static Onboarding create(
            final Member member,
            final ProfileSettingRequest dto
            ) {
        return Onboarding.builder()
                .member(member)
                .purpose(dto.purpose())
                .habits(dto.habits())
                .build();
    }

    public void update(final ProfileSettingRequest dto) {
        updatePurposeAndHabits(dto.purpose(), dto.habits());
    }

    private void updatePurposeAndHabits(final OnboardingPurpose purpose, final List<OnboardingHabit> habits) {
        this.purpose = purpose;
        this.habits = habits == null ? List.of() : habits;
    }

    private void changeMember(final Member member) {
        this.member = member;

        if (Objects.nonNull(member) && member.getOnboarding() != this) {
            member.assignOnboarding(this);
        }
    }
}

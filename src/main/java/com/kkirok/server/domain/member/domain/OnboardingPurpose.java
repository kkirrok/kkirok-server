package com.kkirok.server.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OnboardingPurpose {

    LOSE_WEIGHT("감량"),
    MAINTAIN("유지"),
    GAIN_WEIGHT("증량"),
    HABIT("습관");

    private final String label;

}
package com.kkirok.server.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealStyle {

    BALANCED("균형형"),
    PROTEIN_FOCUSED("단백질 집중형"),
    CARB_PREFERRED("탄수화물 선호형"),
    LATE_NIGHT("야식형"),
    INTERMITTENT_FASTING("간헐적 단식형"),
    DESSERT_OBSESSED("디저트집착유형");

    private final String label;
}

package com.kkirok.server.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OnboardingHabit {

    MEAT("고기"),
    VEGETABLE("채소"),
    LATE_NIGHT_MEAL("야식"),
    SNACK("간식"),
    BIG_EATER("대식"),
    SMALL_EATER("소식"),
    BEVERAGE("음료"),
    DESSERT("디저트"),
    INTERMITTENT_FASTING("간헐적 단식"),
    DIET("다이어트"),
    FAST_FOOD("패스트푸드"),
    DELIVERY_FOOD("배달"),
    SWEET("단맛"),
    SALTY("짠맛"),
    SPICY("매운맛"),
    REGULAR_MEAL("규칙"),
    IRREGULAR_MEAL("불규칙");

    private final String label;
}
package com.kkirok.server.domain.kkinipop.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum KkinipopReactionEmoji {
    HEART("SYSTEM_HEART", "하트"),
    FIRE("SYSTEM_FIRE", "불꽃"),
    PIG("SYSTEM_PIG", "돼지"),
    YUMMY("SYSTEM_YUMMY", "냠냠"),
    SMILE("SYSTEM_SMILE", "미소"),
    DDABONG("SYSTEM_DDABONG", "엄치척");

    private final String code;
    private final String label;

    public static KkinipopReactionEmoji fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown emoji code: " + code));
    }
}

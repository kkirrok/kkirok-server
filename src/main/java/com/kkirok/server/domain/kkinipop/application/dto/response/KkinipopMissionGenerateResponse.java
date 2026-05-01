package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;
import java.util.List;

/**
 * OpenAI가 반환하는 끼니팝 공통 미션 후보 응답값.
 */
public record KkinipopMissionGenerateResponse(
        List<MissionCandidate> missions
) {

    /**
     * 팀 배정 전 공통 후보 미션.
     *
     * <p>예시
     * - title: "점심 식판 자랑하기"
     * - startTime: 12:00
     * - durationMinutes: 10
     */
    public record MissionCandidate(
            String title,
            @JsonProperty("startTime")
            LocalTime startTime,
            @JsonProperty("durationMinutes")
            int durationMinutes
    ) {
    }
}

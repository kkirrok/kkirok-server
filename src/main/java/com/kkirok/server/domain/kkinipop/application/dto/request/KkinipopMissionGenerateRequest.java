package com.kkirok.server.domain.kkinipop.application.dto.request;

import com.kkirok.server.domain.kkinipop.domain.KkinipopMissionPolicy;

import java.time.LocalDate;

/**
 * OpenAI에 전달하는 끼니팝 미션 생성 요청값.
 */
public record KkinipopMissionGenerateRequest(
        LocalDate targetDate,
        int candidateCount,
        int assignedCountPerGroup,
        int timeSlotIntervalMinutes,
        int durationMinutes
) {
    public static KkinipopMissionGenerateRequest create(LocalDate date) {
        return new KkinipopMissionGenerateRequest(
                date,
                KkinipopMissionPolicy.DAILY_MISSION_CANDIDATE_COUNT,
                KkinipopMissionPolicy.DAILY_MISSION_COUNT,
                KkinipopMissionPolicy.REALTIME_SLOT_INTERVAL_MINUTES,
                KkinipopMissionPolicy.REALTIME_DURATION_MINUTES
        );
    }
}

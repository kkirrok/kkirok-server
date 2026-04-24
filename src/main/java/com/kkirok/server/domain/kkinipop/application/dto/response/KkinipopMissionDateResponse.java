package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.global.common.util.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Schema(description = "끼니팝 날짜별 미션 응답")
public record KkinipopMissionDateResponse(
        @Schema(description = "기준 날짜", example = "2026-04-23")
        LocalDate date,
        @Schema(description = "요일 라벨", example = "목")
        String label,
        @Schema(description = "시작 요일 (0:일 ~ 6:토)", example = "4")
        int dayOfWeek,
        @Schema(description = "오늘의 미션 목록")
        List<KkinipopMissionResponse> missions
) {
    public static KkinipopMissionDateResponse from(
            LocalDate date,
            List<KkinipopMission> missions,
            LocalDateTime now,
            Map<Long, List<KkinipopMissionSuccessMemberResponse>> successMembersByMission
    ) {
        List<KkinipopMissionResponse> missionResponses = missions.stream()
                .sorted(Comparator.comparing(com.kkirok.server.domain.kkinipop.domain.KkinipopMission::getStartAt))
                .map(mission -> KkinipopMissionResponse.from(
                        mission,
                        now,
                        successMembersByMission.getOrDefault(mission.getId(), List.of())
                ))
                .toList();

        return new KkinipopMissionDateResponse(
                date,
                DateTimeUtils.toDayLabel(date),
                DateTimeUtils.toDayOfWeekNumber(date),
                missionResponses
        );
    }
}

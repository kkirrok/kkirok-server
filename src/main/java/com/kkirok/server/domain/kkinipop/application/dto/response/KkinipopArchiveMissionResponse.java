package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "끼니팝 모아보기 미션 응답")
public record KkinipopArchiveMissionResponse(
        @Schema(description = "미션 ID", example = "7")
        Long missionId,
        @Schema(description = "미션 제목", example = "10분 안에 과일 올리기")
        String title,
        @Schema(description = "종료 시각", example = "2026-04-22T12:10:00")
        LocalDateTime endedAt,
        @Schema(description = "미션 기록 목록")
        List<KkinipopPostResponse> posts
) {
    public static KkinipopArchiveMissionResponse from(KkinipopMission mission, List<KkinipopPostResponse> posts) {
        return new KkinipopArchiveMissionResponse(
                mission.getId(),
                mission.getTitle(),
                mission.getEndAt(),
                posts
        );
    }
}

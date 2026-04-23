package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "끼니팝 미션 응답")
public record KkinipopMissionResponse(
        @Schema(description = "미션 ID", example = "7")
        Long missionId,
        @Schema(description = "미션 제목", example = "10분 안에 과일 올리기")
        String title,
        @Schema(description = "실시간 미션 여부", example = "true")
        boolean realtime,
        @Schema(description = "현재 실시간 진행 여부", example = "true")
        boolean isLive,
        @Schema(description = "종료 여부", example = "false")
        boolean isEnd,
        @Schema(description = "시작 시각", example = "2026-04-22T12:00:00")
        LocalDateTime startAt,
        @Schema(description = "종료 시각", example = "2026-04-22T12:10:00")
        LocalDateTime endAt,
        @Schema(description = "성공한 멤버 수 ", example = "1")
        int successMemberCount,
        @Schema(description = "성공한 멤버 목록")
        List<KkinipopMissionSuccessMemberResponse> successMembers
) {
    public static KkinipopMissionResponse from(
            KkinipopMission mission,
            LocalDateTime now,
            List<KkinipopMissionSuccessMemberResponse> successMembers
    ) {
        boolean live = mission.isLive(now);
        boolean ended = mission.isEnded(now);

        return new KkinipopMissionResponse(
                mission.getId(),
                mission.getTitle(),
                mission.isRealtime(),
                live,
                ended,
                mission.getStartAt(),
                mission.getEndAt(),
                successMembers.size(),
                successMembers
        );
    }
}

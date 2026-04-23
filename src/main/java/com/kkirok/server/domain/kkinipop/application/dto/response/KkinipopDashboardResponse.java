package com.kkirok.server.domain.kkinipop.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "끼니팝 대시보드 응답")
public record KkinipopDashboardResponse(
        @Schema(description = "조회 날짜", example = "2026-04-22")
        LocalDate date,
        @Schema(description = "현재 선택 그룹")
        KkinipopGroupResponse currentGroup,
        @Schema(description = "참여 중 그룹 목록")
        List<KkinipopGroupResponse> groups,
        @Schema(description = "조회 날짜의 미션 목록")
        List<KkinipopMissionResponse> missions,
        @Schema(description = "그룹 멤버 목록")
        List<KkinipopMemberSummaryResponse> members,
        @Schema(description = "피드 게시글 목록")
        List<KkinipopPostResponse> posts,
        @Schema(description = "커스텀 이모지 목록")
        List<KkinipopCustomEmojiResponse> customEmojis,
        @Schema(description = "기본 리액션 이모지 목록")
        List<KkinipopReactionSummaryResponse> defaultEmojis,
        @Schema(description = "일반 기록 작성 가능 여부", example = "true")
        boolean canWriteGeneralRecord,
        @Schema(description = "미션 기록 작성 가능 여부", example = "true")
        boolean canWriteMissionRecord,
        @Schema(description = "게시글 없음 상태 여부", example = "false")
        boolean empty
) {
}

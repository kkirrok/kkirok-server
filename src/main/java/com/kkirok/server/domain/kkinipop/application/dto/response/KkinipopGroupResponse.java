package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "끼니팝 그룹 응답")
public record KkinipopGroupResponse(
        @Schema(description = "그룹 ID", example = "1")
        Long groupId,
        @Schema(description = "그룹 이름", example = "아침 챌린저스")
        String name,
        @Schema(description = "초대 코드", example = "AB12CD")
        String inviteCode,
        @Schema(description = "참여 인원 수 ", example = "3")
        Integer memberCount,
        @Schema(description = "현재 경험치", example = "50")
        int curExp,
        @Schema(description = "최대 경험치", example = "50")
        int maxExp,
        @Schema(description = "그룹 레벨", example = "1")
        int level,
        @Schema(description = "내가 방장인지 여부", example = "false")
        boolean isLeader
) {
    public static KkinipopGroupResponse from(KkinipopGroupMember membership, int memberCount) {
        return new KkinipopGroupResponse(
                membership.getGroup().getId(),
                membership.getGroup().getName(),
                membership.getGroup().getInviteCode(),
                memberCount,
                0, // TODO: 레벨 정책 적용하기
                100,
                membership.getGroup().getLevel(),
                membership.isLeader()
        );
    }
}

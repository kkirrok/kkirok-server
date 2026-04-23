package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "끼니팝 멤버 요약 응답")
public record KkinipopMemberSummaryResponse(
        @Schema(description = "멤버 ID", example = "12")
        Long memberId,
        @Schema(description = "표시 이름", example = "끼록이")
        String nickname,
        @Schema(description = "프로필 이미지 키", example = "uuid_profileImage")
        String profileImage,
        @Schema(description = "내 프로필 여부", example = "true")
        boolean isMe,
        @Schema(description = "내가 방장인지 방장 여부", example = "false")
        boolean leader
) {
    public static KkinipopMemberSummaryResponse from(KkinipopGroupMember groupMember, Long currentMemberId) {
        return new KkinipopMemberSummaryResponse(
                groupMember.getMember().getId(),
                groupMember.getMember().getDisplayName(),
                groupMember.getMember().getProfileImage(),
                groupMember.getMember().getId().equals(currentMemberId),
                groupMember.isLeader()
        );
    }
}

package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미션 성공 멤버 응답")
public record KkinipopMissionSuccessMemberResponse(
        @Schema(description = "멤버 ID", example = "21")
        Long memberId,
        @Schema(description = "멤버 이름", example = "김준용")
        String name,
        @Schema(description = "프로필 이미지 키", example = "uuid_profileImage")
        String profileImage
) {
    public static KkinipopMissionSuccessMemberResponse from(Member member) {
        return new KkinipopMissionSuccessMemberResponse(
                member.getId(),
                member.getDisplayName(),
                member.getProfileImage()
        );
    }
}

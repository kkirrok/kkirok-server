package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopPost;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "끼니팝 게시글 응답")
public record KkinipopPostResponse(
        @Schema(description = "게시글 ID", example = "11")
        Long postId,
        @Schema(description = "작성자 멤버 ID", example = "21")
        Long memberId,
        @Schema(description = "작성자 표시 이름", example = "끼록이")
        String nickname,
        @Schema(description = "작성자 프로필 이미지 키", example = "uuid_profileImage")
        String profileImage,
        @Schema(description = "사진 이미지 키", example = "uuid_kkinipopPostImage")
        String image,
        @Schema(description = "연결된 미션 ID", example = "7")
        Long missionId,
        @Schema(description = "작성 시각", example = "2026-04-22T12:03:12")
        LocalDateTime createdAt,
        @Schema(description = "리액션 요약 목록")
        List<KkinipopReactionSummaryResponse> reactions
) {
    public static KkinipopPostResponse from(KkinipopPost post, List<KkinipopReactionSummaryResponse> reactions) {
        return new KkinipopPostResponse(
                post.getId(),
                post.getMember().getId(),
                post.getMember().getDisplayName(),
                post.getMember().getProfileImage(),
                post.getImageKey(),
                post.getMission() == null ? null : post.getMission().getId(),
                post.getCreatedAt(),
                reactions
        );
    }
}

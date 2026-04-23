package com.kkirok.server.domain.kkinipop.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "끼니팝 이모지 목록 응답")
public record KkinipopEmojiListResponse(
        @Schema(description = "기본 이모지 목록")
        List<KkinipopEmojiResponse> systemEmojis,
        @Schema(description = "커스텀 이모지 목록")
        List<KkinipopEmojiResponse> customEmojis
) {
}

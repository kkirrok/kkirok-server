package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopReactionEmoji;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "끼니팝 시스템 이모지 enum 응답")
public record KkinipopSystemEmojiResponse(
        @Schema(description = "enum 상수", example = "HEART")
        String value,
        @Schema(description = "시스템 이모지 코드", example = "SYSTEM_HEART")
        String emojiCode,
        @Schema(description = "시스템 이모지 라벨", example = "하트")
        String label
) {
    public static KkinipopSystemEmojiResponse from(KkinipopReactionEmoji emoji) {
        return new KkinipopSystemEmojiResponse(
                emoji.name(),
                emoji.getCode(),
                emoji.getLabel()
        );
    }
}

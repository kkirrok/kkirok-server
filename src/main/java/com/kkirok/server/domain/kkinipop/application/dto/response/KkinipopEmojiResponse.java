package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import com.kkirok.server.domain.kkinipop.domain.KkinipopReactionEmoji;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "끼니팝 이모지 응답")
public record KkinipopEmojiResponse(
        @Schema(description = "이모지 ID. 기본 이모지는 null", example = "5")
        Long emojiId,
        @Schema(description = "이모지 코드", example = "SYSTEM_HEART")
        String emojiCode,
        @Schema(description = "이모지 라벨", example = "하트")
        String label,
        @Schema(description = "이모지 이미지 키. 시스템 이모지는 null", example = "uuid_kkinipopEmojiImage", nullable = true)
        String image,
        @Schema(description = "이모지 타입", example = "SYSTEM_EMOJI")
        String emojiType
) {
    public static KkinipopEmojiResponse ofDefault(KkinipopReactionEmoji emoji) {
        return new KkinipopEmojiResponse(null, emoji.getCode(), emoji.getLabel(), null, "SYSTEM_EMOJI");
    }

    public static KkinipopEmojiResponse ofCustom(KkinipopCustomEmoji customEmoji) {
        return new KkinipopEmojiResponse(customEmoji.getId(), "CUSTOM_" + customEmoji.getId(), customEmoji.getLabel(), customEmoji.getImageKey(), "CUSTOM_EMOJI");
    }

}

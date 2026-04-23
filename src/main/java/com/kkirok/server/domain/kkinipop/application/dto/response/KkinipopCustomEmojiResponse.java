package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "끼니팝 커스텀 이모지 응답")
public record KkinipopCustomEmojiResponse(
        @Schema(description = "커스텀 이모지 ID", example = "5")
        Long customEmojiId,
        @Schema(description = "이모지 코드", example = "CUSTOM_5")
        String emojiCode,
        @Schema(description = "이모지 라벨", example = "chew")
        String label,
        @Schema(description = "이미지 키", example = "uuid_kkinipopEmojiImage")
        String image
) {
    public static KkinipopCustomEmojiResponse from(KkinipopCustomEmoji customEmoji) {
        return new KkinipopCustomEmojiResponse(
                customEmoji.getId(),
                "CUSTOM_" + customEmoji.getId(),
                customEmoji.getLabel(),
                customEmoji.getImageKey()
        );
    }
}

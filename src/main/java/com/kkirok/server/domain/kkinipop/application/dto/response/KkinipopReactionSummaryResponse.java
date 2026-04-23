package com.kkirok.server.domain.kkinipop.application.dto.response;

import com.kkirok.server.domain.kkinipop.domain.KkinipopReaction;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "끼니팝 리액션 요약 응답")
public record KkinipopReactionSummaryResponse(
        @Schema(description = "이모지 코드", example = "SYSTEM_HEART")
        String emojiCode,
        @Schema(description = "이모지 라벨", example = "하트")
        String label,
        @Schema(description = "리액션 수", example = "3")
        long count,
        @Schema(description = "이모지 타입", example = "SYSTEM_EMOJI")
        String emojiType
) {
    public static KkinipopReactionSummaryResponse from(KkinipopReaction reaction, long count) {
        return new KkinipopReactionSummaryResponse(
                reaction.getEmojiCode(),
                reaction.getEmojiLabel(),
                count,
                reaction.isCustomEmoji() ? "CUSTOM_EMOJI" : "SYSTEM_EMOJI"
        );
    }

    public static KkinipopReactionSummaryResponse of(KkinipopReactionSummaryResponse summary, long count) {
        return new KkinipopReactionSummaryResponse(
                summary.emojiCode(),
                summary.label(),
                count,
                summary.emojiType()
        );
    }
}

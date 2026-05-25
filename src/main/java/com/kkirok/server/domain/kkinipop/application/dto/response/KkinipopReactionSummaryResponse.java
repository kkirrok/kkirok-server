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
        String emojiType,
        @Schema(description = "현재 사용자의 리액션 여부", example = "true")
        boolean reacted
) {
    public static KkinipopReactionSummaryResponse from(KkinipopReaction reaction, long count) {
        return from(reaction, count, true);
    }

    public static KkinipopReactionSummaryResponse from(KkinipopReaction reaction, long count, boolean reacted) {
        return new KkinipopReactionSummaryResponse(
                reaction.getEmojiCode(),
                reaction.getEmojiLabel(),
                count,
                reaction.isCustomEmoji() ? "CUSTOM_EMOJI" : "SYSTEM_EMOJI",
                reacted
        );
    }

    public static KkinipopReactionSummaryResponse of(KkinipopReactionSummaryResponse summary, long count, boolean reacted) {
        return new KkinipopReactionSummaryResponse(
                summary.emojiCode(),
                summary.label(),
                count,
                summary.emojiType(),
                reacted
        );
    }
}

package com.kkirok.server.domain.sse.application.dto.payload;

public record SseReactionUpdatedPayload(
        Long groupId,
        Long postId,
        String emojiCode,
        long count,
        boolean reacted,
        Long reactorMemberId
) {
}

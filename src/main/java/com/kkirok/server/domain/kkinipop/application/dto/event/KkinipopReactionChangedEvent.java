package com.kkirok.server.domain.kkinipop.application.dto.event;

public record KkinipopReactionChangedEvent(
        Long postId,
        Long groupId,
        String emojiCode,
        long count,
        boolean reacted
) {
}

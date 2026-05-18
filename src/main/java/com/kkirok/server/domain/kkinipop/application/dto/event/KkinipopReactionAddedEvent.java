package com.kkirok.server.domain.kkinipop.application.dto.event;

public record KkinipopReactionAddedEvent(
        Long postId,
        Long groupId,
        Long postAuthorMemberId,
        Long reactorMemberId,
        String emojiCode,
        boolean customEmoji,
        String customEmojiImageKey
) {
}

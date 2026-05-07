package com.kkirok.server.domain.kkinipop.application.dto.event;

public record KkinipopGroupJoinedEvent(
        Long groupId,
        Long joinedMemberId
) {
}

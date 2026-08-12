package com.kkirok.server.domain.kkinipop.application.dto.event;

public record KkinipopGroupMemberLeftEvent(
        Long groupId,
        Long memberId
) {
}

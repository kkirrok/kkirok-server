package com.kkirok.server.domain.kkinipop.application.dto.event;

public record KkinipopMissionStartedEvent(
        Long groupId,
        Long missionId
) {
}

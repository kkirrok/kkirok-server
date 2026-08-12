package com.kkirok.server.domain.sse.application.dto.payload;

import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionResponse;

public record SseMissionStartedPayload(
        Long groupId,
        KkinipopMissionResponse mission
) {
}

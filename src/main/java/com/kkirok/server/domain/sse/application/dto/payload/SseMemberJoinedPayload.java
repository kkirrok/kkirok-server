package com.kkirok.server.domain.sse.application.dto.payload;

import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMemberSummaryResponse;

public record SseMemberJoinedPayload(
        Long groupId,
        KkinipopMemberSummaryResponse member
) {
}

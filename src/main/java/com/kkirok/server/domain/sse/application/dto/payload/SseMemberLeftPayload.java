package com.kkirok.server.domain.sse.application.dto.payload;

public record SseMemberLeftPayload(
        Long groupId,
        Long memberId
) {
}

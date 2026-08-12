package com.kkirok.server.domain.notification.application.sender;

public record PushBatchMessage(
        String token,
        PushPayload payload
) {
}

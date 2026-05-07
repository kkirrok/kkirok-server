package com.kkirok.server.domain.notification.application.sender;

import java.util.Map;

public record PushPayload(
        String title,
        String body,
        Map<String, String> data
) {
}

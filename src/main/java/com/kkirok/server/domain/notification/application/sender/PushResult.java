package com.kkirok.server.domain.notification.application.sender;

import java.util.List;

public record PushResult(
        int successCount,
        List<String> invalidTokens
) {
}

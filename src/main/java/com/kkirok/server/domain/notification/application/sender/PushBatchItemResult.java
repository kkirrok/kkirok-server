package com.kkirok.server.domain.notification.application.sender;

public record PushBatchItemResult(
        String token,
        boolean success,
        boolean invalidToken
) {
}

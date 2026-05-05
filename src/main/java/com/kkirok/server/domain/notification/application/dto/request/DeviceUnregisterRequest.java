package com.kkirok.server.domain.notification.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceUnregisterRequest(
        @Schema(description = "해제할 FCM 디바이스 토큰", example = "fcm_token_abc123")
        @NotBlank
        String token
) {
}

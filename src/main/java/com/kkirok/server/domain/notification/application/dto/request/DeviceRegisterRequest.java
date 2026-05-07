package com.kkirok.server.domain.notification.application.dto.request;

import com.kkirok.server.domain.notification.domain.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceRegisterRequest(
        @Schema(description = "FCM 디바이스 토큰", example = "fcm_token_abc123")
        @NotBlank
        String token,
        @Schema(description = "디바이스 플랫폼", example = "ANDROID")
        @NotNull
        DevicePlatform platform
) {
}

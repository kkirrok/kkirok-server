package com.kkirok.server.global.external.r2.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedResponse(
        @Schema(description = "R2에 저장되는 키", example = "uuid_profileImage")
        String key,
        @Schema(description = "데이터 다운로드 URL", example = "https://...")
        String downloadUrl
) {
    public static PresignedResponse from(final String key, final String downloadUrl) {
        return new PresignedResponse(key, downloadUrl);
    }
}

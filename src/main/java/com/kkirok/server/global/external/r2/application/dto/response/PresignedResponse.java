package com.kkirok.server.global.external.r2.application.dto.response;

public record PresignedResponse(
    String key,
    String downloadUrl
) {
    public static PresignedResponse from(final String key, final String downloadUrl) {
        return new PresignedResponse(key, downloadUrl);
    }
}

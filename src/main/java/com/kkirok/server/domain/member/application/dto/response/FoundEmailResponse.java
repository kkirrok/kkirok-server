package com.kkirok.server.domain.member.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FoundEmailResponse(
        @Schema(example = "kkirok@test.com", description = "찾은 이메일")
        String email
) {
    public static FoundEmailResponse of(final String email) {
        return new FoundEmailResponse(email);
    }
}

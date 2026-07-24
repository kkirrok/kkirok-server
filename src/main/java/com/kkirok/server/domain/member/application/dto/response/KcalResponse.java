package com.kkirok.server.domain.member.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record KcalResponse(
        @Schema(description = "권장 칼로리(kcal)", example = "2000")
        Integer suggestedKcal
) {
    public static KcalResponse from(Integer suggestedKcal) {
        return new KcalResponse(suggestedKcal);
    }
}

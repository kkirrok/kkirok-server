package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TodayStatusResponse (
        @Schema(description = "에너지", example = "70")
        Integer energy,

        @Schema(description = "건강", example = "82")
        Integer health,

        @Schema(description = "상태 멘트", example = "오늘도 잘 먹고 있어요!")
        String status
){
}

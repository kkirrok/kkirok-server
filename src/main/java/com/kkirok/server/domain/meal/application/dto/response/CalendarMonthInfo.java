package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CalendarMonthInfo(
        @Schema(description = "년도", example = "2026")
        Integer year,
        @Schema(description = "월", example = "1")
        Integer month,
        @Schema(description = "월 시작 날짜", example = "31")
        Integer startDayOfMonth,
        @Schema(description = "월 종료 날짜", example = "31")
        Integer endDayOfMonth,
        @Schema(description = "월 시작 요일", example = "0")
        Integer startDayWeek,
        @Schema(description = "월 종료 요일", example = "6")
        Integer endDayWeek
) {
}

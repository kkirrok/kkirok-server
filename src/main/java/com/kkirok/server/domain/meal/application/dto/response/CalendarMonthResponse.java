package com.kkirok.server.domain.meal.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CalendarMonthResponse(

        @Schema(description = "날짜별 상태정보")
        List<CalendarDayInfo> dayInfos,

        @Schema(description = "해당 월 정보")
        CalendarMonthInfo monthInfo

) {

}

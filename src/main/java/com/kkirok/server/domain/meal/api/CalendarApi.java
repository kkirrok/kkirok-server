package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.response.CalendarMonthResponse;
import com.kkirok.server.domain.meal.exception.CalendarSuccessCode;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Calendar API", description = "달력 관련 API")
public interface CalendarApi {

    @Operation(
            summary = "월간 상태 조회 [USER]",
            description = """
                    특정 월의 상태 정보를 조회합니다.
                    
                    - 월(month) : 월 정보
                    - 요일 : 0(일) ~ 6(토)
                    """
    )
    @ApiErrorCodeExamples({

    })
    @ApiSuccessCodeExample(codeType = CalendarSuccessCode.class, code = "CALENDAR_SUCCESS_CODE")
    ResponseEntity<SuccessResponse<CalendarMonthResponse>> calendarInfo(
            @Parameter(description = "년도 정보", example = "2026", required = true)
            Integer year,
            @Parameter(description = "월 정보 (1 ~ 12)", example = "1", required = true)
            Integer month,
            @Parameter(description = "시작 요일 (0:일 ~ 6:토)", example = "0(일요일)", required = true)
            Integer startDayOfWeek
    );

}

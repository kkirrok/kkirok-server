package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.response.CalendarMonthResponse;
import com.kkirok.server.domain.meal.application.dto.response.DailyMealResponse;
import com.kkirok.server.domain.meal.exception.CalendarSuccessCode;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@Tag(name = "Calendar API", description = "달력 관련 API")
public interface CalendarApi {

    @Operation(
            summary = "월간 상태 조회 [USER]",
            description = """
                    특정 월의 상태 정보를 조회합니다.
                    
                    - 월(month) : 월 정보
                    - 요일 : 0(일) ~ 6(토)
                    - 식단이 기록된 날 → ENERGETIC / 기록 없는 날 → NORMAL
                    """
    )
    @ApiErrorCodeExamples({})
    @ApiSuccessCodeExample(codeType = CalendarSuccessCode.class, code = "CALENDAR_SUCCESS_CODE")
    ResponseEntity<SuccessResponse<CalendarMonthResponse>> calendarInfo(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "년도 정보", example = "2026", required = true)
            Integer year,
            @Parameter(description = "월 정보 (1 ~ 12)", example = "4", required = true)
            Integer month,
            @Parameter(description = "시작 요일 (0:일 ~ 6:토)", example = "0", required = true)
            Integer startDayOfWeek
    );

    @Operation(
            summary = "날짜별 식단 상세 조회 [USER]",
            description = """
                    캘린더에서 특정 날짜를 클릭하면 해당 날짜에 기록된 식단 정보를 반환합니다.
                    
                    - 아침/점심/저녁/간식/야식으로 분류된 식단 목록 반환
                    - 해당 날짜의 영양성분 총합 포함
                    - 식단이 없는 날짜도 빈 리스트로 정상 응답
                    """
    )
    @ApiErrorCodeExamples({})
    @ApiSuccessCodeExample(codeType = CalendarSuccessCode.class, code = "CALENDAR_DAILY_SUCCESS_CODE")
    ResponseEntity<SuccessResponse<DailyMealResponse>> dailyMealInfo(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", example = "2026-04-13", required = true)
            LocalDate date
    );
}

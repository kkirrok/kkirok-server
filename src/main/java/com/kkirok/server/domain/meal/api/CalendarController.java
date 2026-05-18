package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.response.CalendarMonthResponse;
import com.kkirok.server.domain.meal.application.dto.response.DailyMealResponse;
import com.kkirok.server.domain.meal.application.service.CalendarService;
import com.kkirok.server.domain.meal.exception.CalendarSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/calendar")
@RoleUserAuth
public class CalendarController implements CalendarApi {

    private final CalendarService calendarService;

    /**
     * 월간 캘린더 조회
     * GET /v1/calendar?year=2026&month=4&startDayOfWeek=0
     */
    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<CalendarMonthResponse>> calendarInfo(
            @CurrentMember Long memberId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer startDayOfWeek
    ) {
        CalendarMonthResponse response = calendarService.getMonthInfo(memberId, year, month, startDayOfWeek);

        return ResponseEntity.ok(
                SuccessResponse.of(CalendarSuccessCode.CALENDAR_SUCCESS_CODE, response)
        );
    }

    /**
     * 날짜별 식단 상세 조회 (캘린더 날짜 클릭 시 호출)
     * GET /v1/calendar/daily?date=2026-04-13
     */
    @Override
    @GetMapping("/daily")
    public ResponseEntity<SuccessResponse<DailyMealResponse>> dailyMealInfo(
            @CurrentMember Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyMealResponse response = calendarService.getDailyMealInfo(memberId, date);

        return ResponseEntity.ok(
                SuccessResponse.of(CalendarSuccessCode.CALENDAR_DAILY_SUCCESS_CODE, response)
        );
    }
}

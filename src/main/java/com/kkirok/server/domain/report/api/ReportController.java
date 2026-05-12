package com.kkirok.server.domain.report.api;

import com.kkirok.server.domain.meal.exception.MealSuccessCode;
import com.kkirok.server.domain.report.application.dto.response.ReportResponse;
import com.kkirok.server.domain.report.application.dto.response.WeeklyReportResponse;
import com.kkirok.server.domain.report.exception.ReportSuccessCode;
import com.kkirok.server.domain.report.service.WeeklyReportService;
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
@RequestMapping("/v1/reports")
@RoleUserAuth
public class ReportController implements ReportApi {

    private final WeeklyReportService weeklyReportService;

    /**
     * 주간 리포트 조회.
     * weekStart(월요일 날짜)를 넘기면 해당 주, 없으면 이번 주 리포트를 반환합니다.
     * 예) GET /v1/meals/weekly-report?weekStart=2025-04-28
     */
    @Override
    @GetMapping("/weekly-report")
    public ResponseEntity<SuccessResponse<WeeklyReportResponse>> getWeeklyReport(
            @CurrentMember Long memberId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(memberId, weekStart);
        return ResponseEntity.ok(SuccessResponse.of(ReportSuccessCode.WEEKLY_REPORT_GET_SUCCESS, response));
    }
}

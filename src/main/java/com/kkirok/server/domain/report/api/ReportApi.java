package com.kkirok.server.domain.report.api;

import com.kkirok.server.domain.meal.exception.MealSuccessCode;
import com.kkirok.server.domain.report.application.dto.response.ReportResponse;
import com.kkirok.server.domain.report.application.dto.response.WeeklyReportResponse;
import com.kkirok.server.domain.report.exception.ReportErrorCode;
import com.kkirok.server.domain.report.exception.ReportSuccessCode;
import com.kkirok.server.domain.terms.exception.TermsErrorCode;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Report API", description = "주간 리포트 관련 API")
public interface ReportApi {



    @Operation(
            summary = "주간 리포트 조회 [USER]",
            description = """
                    한 주(월~일) 식단 기록을 분석하여 주간 리포트를 반환합니다.
                    
                    - weekStart: 조회할 주의 월요일 날짜 (미입력 시 지난주 월요일 기준)
                    - 종료된 주(지난주 이전)만 조회 가능하며, 진행 중인 이번 주 및 미래 주는 조회할 수 없습니다.
                      (일주일이 끝나야 평균 칼로리 계산이 가능하기 때문)
                    - 응답: 평균 칼로리/영양소, 식사 패턴, 다음 주 제안 2개
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED"),
            @ApiErrorCodeExample(codeType = ReportErrorCode.class, code = "WEEKLY_REPORT_NOT_ENDED")
    })
    @ApiSuccessCodeExample(codeType = MealSuccessCode.class, code = "WEEKLY_REPORT_GET_SUCCESS")
    ResponseEntity<SuccessResponse<WeeklyReportResponse>> getWeeklyReport(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Parameter(description = "조회할 주의 월요일 날짜 (예: 2026-04-28, 지난주 이전만 가능)", example = "2026-04-28")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    );
}
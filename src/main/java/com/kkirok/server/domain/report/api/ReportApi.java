package com.kkirok.server.domain.report.api;

import com.kkirok.server.domain.report.application.dto.response.ReportResponse;
import com.kkirok.server.domain.report.exception.ReportSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Report API", description = "주간 리포트 관련 API")
public interface ReportApi {

    @Operation(
            summary = "주간 리포트 조회 [USER]",
            description = """
                    일주일 식단기록을 분석한 주간 리포트를 조회합니다.
                    
                    - 인증된 사용자 기준으로 조회합니다.
                    - 월요일~일요일 식단기록 분석
                    - 응답: 주간 리포트
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = ReportSuccessCode.class, code = "WEEKLY_REPORT_GET_SUCCESS")
    ResponseEntity<SuccessResponse<ReportResponse>> getWeeklyReport(
            @CurrentMember Long memberId
    );

}

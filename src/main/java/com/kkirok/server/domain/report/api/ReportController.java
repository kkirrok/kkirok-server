package com.kkirok.server.domain.report.api;

import com.kkirok.server.domain.report.application.dto.response.ReportResponse;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/reports")
public class ReportController implements ReportApi {

    @GetMapping("/weekly")
    @Override
    public ResponseEntity<SuccessResponse<ReportResponse>> getWeeklyReport(@CurrentMember Long memberId) {
        return null;
    }
}

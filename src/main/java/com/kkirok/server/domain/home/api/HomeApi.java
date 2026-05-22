package com.kkirok.server.domain.home.api;

import com.kkirok.server.domain.home.application.dto.response.HomeResponse;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Home API", description = "홈 관련 API")
public interface HomeApi {

    @Operation(
            summary = "홈 조회 [USER]",
            description = "현재 로그인한 사용자의 홈 정보를 조회합니다."
    )
    ResponseEntity<HomeResponse> getHome(
            @CurrentMember Long memberId
    );
}

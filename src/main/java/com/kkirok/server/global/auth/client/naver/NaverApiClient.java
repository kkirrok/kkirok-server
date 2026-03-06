package com.kkirok.server.global.auth.client.naver;

import com.kkirok.server.global.auth.client.naver.response.NaverUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "naverApiClient", url = "https://openapi.naver.com")
public interface NaverApiClient {

	@Operation(summary = "네이버 사용자 정보 조회 API", description = "네이버 로그인 결과 사용자 정보를 조회합니다.")
	@GetMapping("/v1/nid/me")
	NaverUserResponse getUserInformation(@RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken);
}


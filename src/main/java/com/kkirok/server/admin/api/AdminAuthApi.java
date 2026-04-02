package com.kkirok.server.admin.api;

import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;
import com.kkirok.server.domain.member.application.dto.response.MemberLoginResponse;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.member.exception.MemberSuccessCode;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin Auth API", description = "관리자 인증 관련 API")
public interface AdminAuthApi {

	@Operation(
			summary = "관리자 로컬 로그인 []",
			description = """
					이메일/비밀번호 기반 관리자 로그인을 수행합니다.

					- `ROLE_ADMIN` 계정만 로그인할 수 있습니다.
					- 일반 사용자 계정으로 로그인하면 실패합니다.
					- refreshToken은 HttpOnly 쿠키로 내려갑니다.
					"""
	)
	@ApiErrorCodeExamples({
			@ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "LOCAL_ACCOUNT_NOT_FOUND"),
			@ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "LOCAL_LOGIN_PASSWORD_MISMATCH"),
			@ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "SOCIAL_ACCOUNT_LOCAL_LOGIN_FORBIDDEN"),
			@ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "DELETED_MEMBER"),
			@ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "ADMIN_LOGIN_FOR_USER_ACCOUNT")
	})
	@ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "ADMIN_LOCAL_LOGIN_SUCCESS")
	ResponseEntity<SuccessResponse<MemberLoginResponse>> adminLocalLogin(
			@Valid @RequestBody LocalLoginRequest request,
			HttpServletResponse httpServletResponse
	);
}

package com.kkirok.server.admin.api;

import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;
import com.kkirok.server.domain.member.application.dto.response.LoginSuccessResponse;
import com.kkirok.server.domain.member.application.dto.response.MemberLoginResponse;
import com.kkirok.server.domain.member.application.service.LocalLoginService;
import com.kkirok.server.domain.member.exception.MemberSuccessCode;
import com.kkirok.server.global.common.dto.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminAuthController implements AdminAuthApi {

	private static final String REFRESH_TOKEN = "refreshToken";
	private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

	private final LocalLoginService localLoginService;

	@Override
	@PostMapping("/login")
	public ResponseEntity<SuccessResponse<MemberLoginResponse>> adminLocalLogin(
			@Valid @RequestBody LocalLoginRequest request,
			HttpServletResponse httpServletResponse
	) {
		LoginSuccessResponse loginSuccessResponse = localLoginService.adminLogin(request);
		writeRefreshTokenCookie(httpServletResponse, loginSuccessResponse.refreshToken());
		return ResponseEntity.ok().body(
				SuccessResponse.of(
						MemberSuccessCode.ADMIN_LOCAL_LOGIN_SUCCESS,
						MemberLoginResponse.of(
								loginSuccessResponse.accessToken(),
								loginSuccessResponse.nickname(),
								loginSuccessResponse.role()
						)
				)
		);
	}

	private void writeRefreshTokenCookie(HttpServletResponse httpServletResponse, String refreshToken) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN, refreshToken)
				.maxAge(COOKIE_MAX_AGE)
				.path("/")
				.secure(true)
				.sameSite("None")
				.httpOnly(true)
				.build();
		httpServletResponse.setHeader("Set-Cookie", cookie.toString());
	}
}

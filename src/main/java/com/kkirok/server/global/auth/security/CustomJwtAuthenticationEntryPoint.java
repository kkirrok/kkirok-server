package com.kkirok.server.global.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws java.io.IOException {
		String path = request.getRequestURI();
		String method = request.getMethod();
		log.warn("Unauthorized access attempt: Method: {}, Path: {}, Message: {}", method, path,
			authException.getMessage());

		setResponse(response, MemberErrorCode.AUTHENTICATION_REQUIRED);
	}

	private void setResponse(HttpServletResponse response, MemberErrorCode errorCode) throws java.io.IOException {
		response.setStatus(errorCode.getStatus());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), ErrorResponse.from(errorCode));
	}
}
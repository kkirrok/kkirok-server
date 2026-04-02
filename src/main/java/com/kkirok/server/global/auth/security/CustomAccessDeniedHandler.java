package com.kkirok.server.global.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {
		String path = request.getRequestURI();
		String method = request.getMethod();
		log.warn("Access Denied: Method: {}, Path: {}, Message: {}", method, path, accessDeniedException.getMessage());

		setResponse(response, MemberErrorCode.INVALID_ROLE);
    }

	private void setResponse(HttpServletResponse response, MemberErrorCode errorCode) throws IOException {
		response.setStatus(errorCode.getStatus());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), ErrorResponse.from(errorCode));
    }
}

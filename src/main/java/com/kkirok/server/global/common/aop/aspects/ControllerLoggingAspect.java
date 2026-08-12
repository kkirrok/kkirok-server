package com.kkirok.server.global.common.aop.aspects;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Order(2)
@Component
@Profile("!test")
public class ControllerLoggingAspect {

	private static final String REQUEST_URI = "requestURI";
	private static final String CONTROLLER = "controller";
	private static final String METHOD = "method";
	private static final String HTTP_METHOD = "httpMethod";
	private static final String LOG_TIME = "logTime";
	private static final String PARAMS = "params";
	private static final String START_TIME_ATTRIBUTE = "controllerLoggingStartTime";

	/** Controller 요청 로깅 */
	@Before("com.kkirok.server.global.common.aop.Pointcuts.allController()")
	public void logControllerRequest(JoinPoint joinPoint) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) return;

		HttpServletRequest request = attributes.getRequest();
		Map<String, Object> logInfo = new HashMap<>();

		logInfo.put(CONTROLLER, joinPoint.getSignature().getDeclaringType().getSimpleName());
		logInfo.put(METHOD, joinPoint.getSignature().getName());
		logInfo.put(PARAMS, getParams(request));
		logInfo.put(LOG_TIME, System.currentTimeMillis());
		logInfo.put(HTTP_METHOD, request.getMethod());
		request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

		try {
			logInfo.put(REQUEST_URI, URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8));
		} catch (Exception e) {
			logInfo.put(REQUEST_URI, request.getRequestURI());
			log.error("[로깅 에러] URL 디코딩 실패", e);
		}

		log.info("[HTTP {}] {} | {}.{}() | Params: {}",
			logInfo.get(HTTP_METHOD), logInfo.get(REQUEST_URI),
			logInfo.get(CONTROLLER), logInfo.get(METHOD),
			logInfo.get(PARAMS));
	}

	/** Controller 정상 반환 로깅 - 응답 본문은 민감정보 재노출 방지를 위해 찍지 않고 status/소요시간만 남긴다 */
	@AfterReturning(value = "com.kkirok.server.global.common.aop.Pointcuts.allController()", returning = "result")
	public void logControllerResponse(JoinPoint joinPoint, Object result) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) return;

		HttpServletRequest request = attributes.getRequest();
		long elapsed = resolveElapsedMillis(request);
		String status = resolveStatus(result);

		log.info("[HTTP {}] {} {} | {}.{}() | {}ms",
			status, request.getMethod(), request.getRequestURI(),
			joinPoint.getSignature().getDeclaringType().getSimpleName(),
			joinPoint.getSignature().getName(),
			elapsed);
	}

	private long resolveElapsedMillis(HttpServletRequest request) {
		Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
		if (!(startTime instanceof Long start)) {
			return -1;
		}
		return System.currentTimeMillis() - start;
	}

	private String resolveStatus(Object result) {
		if (result instanceof ResponseEntity<?> responseEntity) {
			return String.valueOf(responseEntity.getStatusCode().value());
		}
		return "200";
	}

	/** HTTP 요청 파라미터를 JSON 형태로 변환 */
	private static JSONObject getParams(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		Enumeration<String> params = request.getParameterNames();

		while (params.hasMoreElements()) {
			String param = params.nextElement();
			String replacedParam = param.replace(".", "-");
			String[] values = request.getParameterValues(param);

			if (values == null || values.length == 0) {
				jsonObject.put(replacedParam, ""); // 값이 없을 경우 빈 문자열 저장
			} else if (values.length > 1) {
				jsonObject.put(replacedParam, values); // 여러 값이 있는 경우 배열로 저장
			} else {
				jsonObject.put(replacedParam, values[0]); // 단일 값이면 문자열로 저장
			}
		}
		return jsonObject;
	}
}

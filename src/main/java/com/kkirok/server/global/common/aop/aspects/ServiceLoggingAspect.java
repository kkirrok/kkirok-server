package com.kkirok.server.global.common.aop.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Order(3)
@Component
@Profile("!test")
public class ServiceLoggingAspect {

	/** Service 메서드 실행 전 로깅 */
	@Before("com.kkirok.server.global.common.aop.Pointcuts.allService()")
	public void doLog(JoinPoint joinPoint) {
		log.info("[메서드 실행] {}.{}() | 인자: {}",
			joinPoint.getSignature().getDeclaringType().getSimpleName(),
			joinPoint.getSignature().getName(),
			Arrays.toString(joinPoint.getArgs()));
	}

	/** Service 정상 반환 로깅 */
	@AfterReturning(value = "com.kkirok.server.global.common.aop.Pointcuts.allService()", returning = "result")
	public void logReturn(JoinPoint joinPoint, Object result) {
		log.debug("[Service 정상 반환] {}.{}() | 반환 값: {}",
			joinPoint.getSignature().getDeclaringType().getSimpleName(),
			joinPoint.getSignature().getName(),
			result);
	}

	/** 예외 발생 시 로깅 */
	@AfterThrowing(value = "com.kkirok.server.global.common.aop.Pointcuts.allService()", throwing = "ex")
	public void logException(JoinPoint joinPoint, Exception ex) {
		log.error("[예외 발생] {}.{}() | 예외 메시지: {}",
			joinPoint.getSignature().getDeclaringType().getSimpleName(),
			joinPoint.getSignature().getName(),
			ex.getMessage());
	}

	/** 메서드 실행 후 로깅 */
	@After("com.kkirok.server.global.common.aop.Pointcuts.allService()")
	public void doAfter(JoinPoint joinPoint) {
		log.info("[메서드 종료] {}.{}()",
			joinPoint.getSignature().getDeclaringType().getSimpleName(),
			joinPoint.getSignature().getName());
	}
}

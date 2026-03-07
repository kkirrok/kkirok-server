package com.kkirok.server.global.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *  Swagger 문서 노출용 엔드포인트에서 인증/인가 검증을 생략하기 위해 사용하는 마커 어노테이션입니다.
 *  이 어노테이션이 선언된 메서드는 Swagger 전용 보안 예외 처리 대상입니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisableSwaggerSecurity {}

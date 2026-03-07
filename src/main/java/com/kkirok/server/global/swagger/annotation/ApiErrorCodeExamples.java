package com.kkirok.server.global.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger 에러 응답 예시(복수) 지정 어노테이션.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExamples {

    ApiErrorCodeExample[] value();
}

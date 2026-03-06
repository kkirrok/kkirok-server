package com.kkirok.server.global.swagger.annotation;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger 에러 응답 예시(단일) 지정 어노테이션.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExample {

    Class<? extends BaseErrorCode> codeType() default BaseErrorCode.class;

    String code() default "";

    int status() default -1;

    String message() default "";

    String exampleName() default "";
}

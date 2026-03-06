package com.kkirok.server.global.swagger.annotation;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger 성공 응답 예시(단일) 지정 어노테이션.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSuccessCodeExample {

    Class<? extends BaseSuccessCode> codeType() default BaseSuccessCode.class;

    String code() default "";

    int status() default -1;

    String description() default "";

    String exampleName() default "SUCCESS";

    String example() default "";
}

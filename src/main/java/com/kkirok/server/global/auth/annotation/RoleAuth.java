package com.kkirok.server.global.auth.annotation;

import com.kkirok.server.domain.user.domain.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Role을 기반으로 권한을 체크하는 커스텀 어노테이션
    role에 검사할 역할을 지정해줘야 합니다. ( 한 개 이상 )
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleAuth {

    Role[] role();

}

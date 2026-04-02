package com.kkirok.server.global.auth.interceptor;

import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.annotation.RoleAuth;
import com.kkirok.server.global.common.exception.UnauthorizedException;
import com.kkirok.server.global.common.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 형식 검사
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 인증정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // role 정보 추출. 없으면 통과 ( 권한 필요 없다는 것 )
        RoleAuth roleAuth = resolveRoleAuth(handlerMethod);
        if (roleAuth == null) {
            return true;
        }

        // 검증
        validateAuthentication(authentication);
        validateRoles(roleAuth.role(), authentication.getAuthorities());
        return true;
    }

    private RoleAuth resolveRoleAuth(HandlerMethod handlerMethod) {
        RoleAuth roleAuth = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RoleAuth.class);
        if (roleAuth != null) {
            return roleAuth;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RoleAuth.class);
    }

    // 인증정보 검증
    private void validateAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(MemberErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    // 역할(권한) 검증
    private void validateRoles(Role[] roles, Collection<? extends GrantedAuthority> authorities){
        for (Role role : roles) {
            boolean matched = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals(role.getRoleName()));
            if (matched) {
                return;
            }
        }
        throw new ForbiddenException(MemberErrorCode.INVALID_ROLE, makeRolesString(roles));
    }

    private String makeRolesString(Role[] roles) {
        return "필요한 권한 : [ " +
                Arrays.stream(roles)
                        .map(Role::getRoleName)
                        .collect(Collectors.joining(", ")) +
                " ]";
    }

}

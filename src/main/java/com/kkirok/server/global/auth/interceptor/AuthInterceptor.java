package com.kkirok.server.global.auth.interceptor;

import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.terms.application.service.TermsService;
import com.kkirok.server.domain.terms.exception.TermsErrorCode;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.annotation.RoleAuth;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.auth.annotation.TermsCheckExempt;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.UnauthorizedException;
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
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final MemberUseCase memberUseCase;
    private final TermsService termsService;

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
        validateRoles(roleAuth.role(), resolveCurrentRole(authentication));
        validateActiveMember(handlerMethod, authentication);
        validateTermsAgreement(handlerMethod, authentication);
        return true;
    }

    private RoleAuth resolveRoleAuth(HandlerMethod handlerMethod) {
        RoleAuth roleAuth = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RoleAuth.class);
        if (roleAuth != null) {
            return roleAuth;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RoleAuth.class);
    }

    private void validateActiveMember(HandlerMethod handlerMethod, Authentication authentication) {
        if (hasRoleUserAuth(handlerMethod)) {
            Long memberId = Long.valueOf(authentication.getPrincipal().toString());
            memberUseCase.findMemberByMemberId(memberId); // findMemberByMemberId에서 예외 발생시킴
        }
    }

    private boolean hasRoleUserAuth(HandlerMethod handlerMethod) {
        if (AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RoleUserAuth.class) != null) {
            return true;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RoleUserAuth.class) != null;
    }

    private void validateTermsAgreement(HandlerMethod handlerMethod, Authentication authentication) {
        if (isTermsCheckExempt(handlerMethod)) {
            return;
        }
        if (resolveCurrentRole(authentication) == Role.ADMIN) {
            return;
        }
        Long memberId = Long.valueOf(authentication.getPrincipal().toString());
        if (termsService.hasPendingRequiredTerms(memberId)) {
            throw new ForbiddenException(TermsErrorCode.TERMS_AGREEMENT_REQUIRED);
        }
    }

    private boolean isTermsCheckExempt(HandlerMethod handlerMethod) {
        if (AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), TermsCheckExempt.class) != null) {
            return true;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), TermsCheckExempt.class) != null;
    }

    // 인증정보 검증
    private void validateAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(MemberErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    // 역할(권한) 검증
    private void validateRoles(Role[] roles, Role currentRole){
        for (Role role : roles) {
            if (role == currentRole) {
                return;
            }
        }
        throw new ForbiddenException(MemberErrorCode.INVALID_ROLE, makeRolesString(roles));
    }

    private Role resolveCurrentRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::toRole)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException(MemberErrorCode.AUTHENTICATION_REQUIRED));
    }

    private Role toRole(String authority) {
        String roleName = authority.replace("ROLE_", "");
        return Role.valueOf(roleName);
    }

    private String makeRolesString(Role[] roles) {
        return "필요한 권한 : [ " +
                Arrays.stream(roles)
                        .map(Role::getRoleName)
                        .collect(Collectors.joining(", ")) +
                " ]";
    }

}

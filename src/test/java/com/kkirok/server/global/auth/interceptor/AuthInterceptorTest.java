package com.kkirok.server.global.auth.interceptor;

import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.terms.application.service.TermsService;
import com.kkirok.server.domain.terms.exception.TermsErrorCode;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.annotation.RoleAdminAuth;
import com.kkirok.server.global.auth.annotation.RoleAuth;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.auth.annotation.TermsCheckExempt;
import com.kkirok.server.global.auth.jwt.application.RoleCacheService;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private RoleCacheService roleCacheService;

    @Mock
    private TermsService termsService;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("@RoleUserAuth API에 탈퇴 회원이 요청하면 NotFoundException이 발생한다")
    void shouldThrowNotFoundException_whenDeletedMemberRequestsRoleUserAuth() throws Exception {
        // Given
        Long memberId = 1L;
        setAuthentication(memberId, Role.USER);
        HandlerMethod handlerMethod = handlerMethod("userApi");
        given(roleCacheService.getRole(memberId))
                .willThrow(new NotFoundException(MemberErrorCode.DELETED_MEMBER));

        // When, Then
        assertThatThrownBy(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.DELETED_MEMBER);
    }

    @Test
    @DisplayName("@RoleUserAuth API에 정상 회원이 요청하면 통과한다")
    void shouldPass_whenActiveMemberRequestsRoleUserAuth() throws Exception {
        // Given
        Long memberId = 1L;
        setAuthentication(memberId, Role.USER);
        HandlerMethod handlerMethod = handlerMethod("userApi");
        given(roleCacheService.getRole(memberId)).willReturn(Role.USER);
        given(termsService.hasPendingRequiredTerms(memberId)).willReturn(false);

        // When, Then
        assertThatCode(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .doesNotThrowAnyException();
        then(roleCacheService).should().getRole(memberId);
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않은 회원이 일반 API를 호출하면 ForbiddenException이 발생한다")
    void shouldThrowForbiddenException_whenMemberHasPendingRequiredTerms() throws Exception {
        // Given
        setAuthentication(1L, Role.USER);
        HandlerMethod handlerMethod = handlerMethod("userApi");
        given(termsService.hasPendingRequiredTerms(1L)).willReturn(true);

        // When, Then
        assertThatThrownBy(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .isInstanceOf(ForbiddenException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TermsErrorCode.TERMS_AGREEMENT_REQUIRED);
    }

    @Test
    @DisplayName("필수 약관에 모두 동의한 회원은 통과한다")
    void shouldPass_whenMemberHasNoPendingRequiredTerms() throws Exception {
        // Given
        setAuthentication(1L, Role.USER);
        HandlerMethod handlerMethod = handlerMethod("userApi");
        given(termsService.hasPendingRequiredTerms(1L)).willReturn(false);

        // When, Then
        assertThatCode(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("@TermsCheckExempt API는 약관 미동의 상태여도 통과한다")
    void shouldPassWithoutTermsCheck_whenTermsCheckExemptApiIsRequested() throws Exception {
        // Given
        setAuthentication(1L, Role.USER);
        HandlerMethod handlerMethod = handlerMethod("termsCheckExemptApi");

        // When, Then
        assertThatCode(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .doesNotThrowAnyException();
        then(termsService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("ADMIN 역할은 약관 미동의 상태여도 통과한다")
    void shouldPassWithoutTermsCheck_whenAdminRequestsApi() throws Exception {
        // Given
        setAuthentication(1L, Role.ADMIN);
        HandlerMethod handlerMethod = handlerMethod("adminApi");

        // When, Then
        assertThatCode(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .doesNotThrowAnyException();
        then(termsService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("@RoleAdminAuth API에는 탈퇴 회원 체크를 적용하지 않는다")
    void shouldNotCheckDeletedMember_whenRoleAdminAuthApiIsRequested() throws Exception {
        // Given
        setAuthentication(1L, Role.ADMIN);
        HandlerMethod handlerMethod = handlerMethod("adminApi");

        // When, Then
        assertThatCode(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .doesNotThrowAnyException();
        then(roleCacheService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("@RoleAuth만 직접 붙은 API에는 탈퇴 회원 체크를 적용하지 않는다")
    void shouldNotCheckDeletedMember_whenDirectRoleAuthApiIsRequested() throws Exception {
        // Given
        setAuthentication(1L, Role.USER);
        HandlerMethod handlerMethod = handlerMethod("directRoleAuthApi");

        // When, Then
        assertThatCode(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .doesNotThrowAnyException();
        then(roleCacheService).shouldHaveNoInteractions();
    }

    private void setAuthentication(Long memberId, Role role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                memberId.toString(),
                null,
                List.of(role.toGrantedAuthority())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(controller, method);
    }

    private static class TestController {

        @RoleUserAuth
        public void userApi() {
        }

        @RoleAdminAuth
        public void adminApi() {
        }

        @RoleAuth(role = Role.USER)
        public void directRoleAuthApi() {
        }

        @RoleUserAuth
        @TermsCheckExempt
        public void termsCheckExemptApi() {
        }
    }
}

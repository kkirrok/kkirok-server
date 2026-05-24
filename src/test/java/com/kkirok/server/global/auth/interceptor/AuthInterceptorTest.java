package com.kkirok.server.global.auth.interceptor;

import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.annotation.RoleAdminAuth;
import com.kkirok.server.global.auth.annotation.RoleAuth;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
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
    private MemberUseCase memberUseCase;

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
        given(memberUseCase.findMemberByMemberId(memberId))
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

        // When, Then
        assertThatCode(() -> authInterceptor.preHandle(null, null, handlerMethod))
                .doesNotThrowAnyException();
        then(memberUseCase).should().findMemberByMemberId(memberId);
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
        then(memberUseCase).shouldHaveNoInteractions();
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
        then(memberUseCase).shouldHaveNoInteractions();
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
    }
}

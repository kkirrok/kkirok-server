package com.kkirok.server.domain.member.api;

import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;
import com.kkirok.server.domain.member.application.dto.request.EmailVerificationConfirmRequest;
import com.kkirok.server.domain.member.application.dto.request.EmailVerificationSendRequest;
import com.kkirok.server.domain.member.application.dto.request.LocalSignUpRequest;
import com.kkirok.server.domain.member.application.dto.response.AccessTokenGenerateResponse;
import com.kkirok.server.domain.member.application.dto.response.EmailVerificationStatusResponse;
import com.kkirok.server.domain.member.application.dto.response.LoginSuccessResponse;
import com.kkirok.server.domain.member.application.dto.response.MemberLoginResponse;
import com.kkirok.server.domain.member.application.service.AuthenticationService;
import com.kkirok.server.domain.member.application.service.EmailVerificationService;
import com.kkirok.server.domain.member.application.service.EmailVerificationStateService;
import com.kkirok.server.domain.member.application.service.LocalLoginService;
import com.kkirok.server.domain.member.application.service.SocialLoginService;
import com.kkirok.server.domain.member.exception.MemberSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;
import com.kkirok.server.global.auth.jwt.application.TokenService;
import com.kkirok.server.global.common.dto.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final TokenService tokenService;
    private final AuthenticationService authenticationService;
    private final SocialLoginService socialLoginService;
    private final LocalLoginService localLoginService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationStateService emailVerificationStateService;

    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

    @Override
    @PostMapping("/sign-up")
    public ResponseEntity<SuccessResponse<MemberLoginResponse>> signUp(
            @RequestParam final String authorizationCode,
            @RequestBody final MemberLoginRequest loginRequest,
            HttpServletResponse httpServletResponse
    ) {
        LoginSuccessResponse loginSuccessResponse = socialLoginService.handleSocialLogin(authorizationCode,
                loginRequest);
        writeRefreshTokenCookie(httpServletResponse, loginSuccessResponse.refreshToken());
        MemberLoginResponse response = toMemberLoginResponse(loginSuccessResponse);
        return ResponseEntity.ok()
                .body(SuccessResponse.of(MemberSuccessCode.SOCIAL_LOGIN_SUCCESS, response));
    }

    @Override
    @PostMapping("/email-verification/send")
    public ResponseEntity<SuccessResponse<Void>> sendEmailVerificationCode(
            @Valid @RequestBody final EmailVerificationSendRequest request
    ) {
        emailVerificationService.sendVerificationCode(request.email());
        return ResponseEntity.ok()
                .body(SuccessResponse.from(MemberSuccessCode.EMAIL_VERIFICATION_CODE_SENT));
    }

    @Override
    @PostMapping("/email-verification/verify")
    public ResponseEntity<SuccessResponse<EmailVerificationStatusResponse>> verifyEmail(
            @Valid @RequestBody final EmailVerificationConfirmRequest request
    ) {
        emailVerificationStateService.verify(request.email(), request.code());
        return ResponseEntity.ok()
                .body(SuccessResponse.of(
                        MemberSuccessCode.EMAIL_VERIFIED_SUCCESS,
                        EmailVerificationStatusResponse.of(request.email(), true)
                ));
    }

    @Override
    @PostMapping("/local/sign-up")
    public ResponseEntity<SuccessResponse<MemberLoginResponse>> localSignUp(
            @Valid @RequestBody final LocalSignUpRequest request,
            HttpServletResponse httpServletResponse
    ) {
        LoginSuccessResponse loginSuccessResponse = localLoginService.signUp(request);
        writeRefreshTokenCookie(httpServletResponse, loginSuccessResponse.refreshToken());
        MemberLoginResponse response = toMemberLoginResponse(loginSuccessResponse);
        return ResponseEntity.ok()
                .body(SuccessResponse.of(MemberSuccessCode.LOCAL_SIGN_UP_SUCCESS, response));
    }

    @Override
    @PostMapping("/local/login")
    public ResponseEntity<SuccessResponse<MemberLoginResponse>> localLogin(
            @Valid @RequestBody final LocalLoginRequest request,
            HttpServletResponse httpServletResponse
    ) {
        LoginSuccessResponse loginSuccessResponse = localLoginService.login(request);
        writeRefreshTokenCookie(httpServletResponse, loginSuccessResponse.refreshToken());
        MemberLoginResponse response = toMemberLoginResponse(loginSuccessResponse);
        return ResponseEntity.ok()
                .body(SuccessResponse.of(MemberSuccessCode.LOCAL_LOGIN_SUCCESS, response));
    }

    private void writeRefreshTokenCookie(HttpServletResponse httpServletResponse, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .maxAge(COOKIE_MAX_AGE)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();
        httpServletResponse.setHeader("Set-Cookie", cookie.toString());
    }

    private MemberLoginResponse toMemberLoginResponse(LoginSuccessResponse loginSuccessResponse) {
        return MemberLoginResponse.of(loginSuccessResponse.accessToken(),
                loginSuccessResponse.nickname(),
                loginSuccessResponse.role());
    }

    @Override
    @GetMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<AccessTokenGenerateResponse>> issueAccessTokenUsingRefreshToken(
            @CookieValue(value = REFRESH_TOKEN) final String refreshToken
    ) {
        AccessTokenGenerateResponse response = authenticationService.generateAccessTokenFromRefreshToken(refreshToken);
        return ResponseEntity.ok()
                .body(SuccessResponse.of(MemberSuccessCode.ISSUE_ACCESS_TOKEN_USING_REFRESH_TOKEN, response));
    }

    @Override
    @PostMapping("/sign-out")
    public ResponseEntity<SuccessResponse<Void>> signOut(
            @CurrentMember final Long memberId
    ) {
        tokenService.deleteRefreshToken(memberId);
        return ResponseEntity.ok()
                .body(SuccessResponse.from(MemberSuccessCode.SIGN_OUT_SUCCESS));
    }
}

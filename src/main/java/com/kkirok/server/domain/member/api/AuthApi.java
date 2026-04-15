package com.kkirok.server.domain.member.api;

import com.kkirok.server.domain.member.application.dto.request.EmailVerificationConfirmRequest;
import com.kkirok.server.domain.member.application.dto.request.EmailVerificationSendRequest;
import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;
import com.kkirok.server.domain.member.application.dto.request.LocalSignUpRequest;
import com.kkirok.server.domain.member.application.dto.response.AccessTokenGenerateResponse;
import com.kkirok.server.domain.member.application.dto.response.CurrentRoleResponse;
import com.kkirok.server.domain.member.application.dto.response.EmailVerificationStatusResponse;
import com.kkirok.server.domain.member.application.dto.response.MemberLoginResponse;
import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.member.exception.MemberSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;
import com.kkirok.server.global.auth.jwt.exception.TokenErrorCode;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.common.redis.exception.RedisErrorCode;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User Auth API", description = "일반 사용자 인증 관련 API")
public interface AuthApi {

    String REFRESH_TOKEN = "refreshToken";

    @Operation(
            summary = "소셜 로그인/회원가입 []",
            description = """
                    소셜 authorizationCode로 로그인합니다.
                    회원이 없으면 자동으로 회원가입 후 로그인 처리됩니다.

                    - 요청 파라미터: `authorizationCode`
                    - 요청 바디: `socialType`, `state(NAVER 필수)`
                    - 응답: accessToken + 사용자 정보
                    - refreshToken은 HttpOnly 쿠키로 내려갑니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "SOCIAL_TYPE_BAD_REQUEST"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "AUTHENTICATION_CODE_EXPIRED")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "SOCIAL_LOGIN_SUCCESS")
    ResponseEntity<SuccessResponse<MemberLoginResponse>> signUp(
            @Parameter(description = "소셜 인증 코드", required = true)
            @RequestParam final String authorizationCode,
            @RequestBody final MemberLoginRequest loginRequest,
            HttpServletResponse httpServletResponse
    );

    @Operation(
            summary = "이메일 인증번호 발송 []",
            description = """
                    입력한 이메일로 숫자 6자리 인증번호를 발송합니다.

                    - 요청 바디: `email`
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = EmailErrorCode.class, code = "EMAIL_SEND_FAILED"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_SAVE_FAILED"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_DELETE_FAILED")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "EMAIL_VERIFICATION_CODE_SENT")
    ResponseEntity<SuccessResponse<Void>> sendEmailVerificationCode(
            @Valid @RequestBody final EmailVerificationSendRequest request
    );

    @Operation(
            summary = "이메일 인증번호 확인 []",
            description = """
                    이메일과 숫자 6자리 인증번호를 검증합니다.

                    - 요청 바디: `email`, `code`
                    - 응답: 이메일 인증 완료 여부
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = EmailErrorCode.class, code = "EMAIL_VERIFICATION_CODE_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = EmailErrorCode.class, code = "EMAIL_VERIFICATION_CODE_MISMATCH"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_READ_FAILED"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_SAVE_FAILED"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_DELETE_FAILED")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "EMAIL_VERIFIED_SUCCESS")
    ResponseEntity<SuccessResponse<EmailVerificationStatusResponse>> verifyEmail(
            @Valid @RequestBody final EmailVerificationConfirmRequest request
    );

    @Operation(
            summary = "로컬 회원가입 []",
            description = """
                    이메일/비밀번호 기반 로컬 회원가입을 수행합니다.
                    회원가입 성공 시 즉시 로그인 처리됩니다.
                    
                    회원가입 후, 초기 권한은 PENDING 입니다. USER 권한을 획득하기 위해서는 프로필을 설정해야 합니다.
                    프로필을 설정하지 않으면 서비스를 이용할 수 없습니다.( PENDING은 권한이 부족함 )

                    - 요청 바디: `email`, `password`
                    - 응답: accessToken + 사용자 정보
                    - refreshToken은 HttpOnly 쿠키로 내려갑니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "LOCAL_EMAIL_ALREADY_EXISTS"),
            @ApiErrorCodeExample(codeType = EmailErrorCode.class, code = "EMAIL_NOT_VERIFIED"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_READ_FAILED"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_DELETE_FAILED")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "LOCAL_SIGN_UP_SUCCESS")
    ResponseEntity<SuccessResponse<MemberLoginResponse>> localSignUp(
            @Valid @RequestBody final LocalSignUpRequest request,
            HttpServletResponse httpServletResponse
    );

    @Operation(
            summary = "로컬 로그인 []",
            description = """
                    이메일/비밀번호 기반 로컬 로그인을 수행합니다.

                    - 요청 바디: `email`, `password`
                    - 응답: accessToken + 사용자 정보
                    - refreshToken은 HttpOnly 쿠키로 내려갑니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "LOCAL_ACCOUNT_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "LOCAL_LOGIN_PASSWORD_MISMATCH"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "SOCIAL_ACCOUNT_LOCAL_LOGIN_FORBIDDEN"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "DELETED_MEMBER"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "USER_LOGIN_FOR_ADMIN_ACCOUNT")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "LOCAL_LOGIN_SUCCESS")
    ResponseEntity<SuccessResponse<MemberLoginResponse>> userLocalLogin(
            @Valid @RequestBody final LocalLoginRequest request,
            HttpServletResponse httpServletResponse
    );

    @Operation(
            summary = "액세스 토큰 재발급 []",
            description = """
                    `refreshToken` 쿠키를 검증해 액세스 토큰을 재발급합니다.

                    - 요청 쿠키: `refreshToken`
                    - 응답: 신규 accessToken
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "REFRESH_TOKEN_EXPIRED_ERROR"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "INVALID_REFRESH_TOKEN_ERROR"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "REFRESH_TOKEN_SIGNATURE_ERROR"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "UNSUPPORTED_REFRESH_TOKEN_ERROR"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "REFRESH_TOKEN_EMPTY_ERROR"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "REFRESH_TOKEN_MEMBER_ID_MISMATCH_ERROR"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "REFRESH_TOKEN_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "UNKNOWN_REFRESH_TOKEN_ERROR")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "ISSUE_ACCESS_TOKEN_USING_REFRESH_TOKEN")
    ResponseEntity<SuccessResponse<AccessTokenGenerateResponse>> issueAccessTokenUsingRefreshToken(
            @Parameter(description = "리프레시 토큰 쿠키", required = true)
            @CookieValue(value = REFRESH_TOKEN) final String refreshToken
    );

    @Operation(
            summary = "현재 권한 조회 [PENDING, USER, ADMIN]",
            description = """
                    현재 액세스 토큰에 담긴 사용자 권한을 조회합니다.

                    - 응답: `role`, `authority`
                    """
    )
    @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED")
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "CURRENT_ROLE_GET_SUCCESS")
    ResponseEntity<SuccessResponse<CurrentRoleResponse>> getCurrentRole(
            @CurrentMember final Long memberId
    );

    @Operation(
            summary = "로그아웃 [USER, ADMIN]",
            description = """
                    현재 로그인 사용자의 refreshToken을 서버 저장소에서 삭제합니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "REFRESH_TOKEN_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "SIGN_OUT_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> signOut(
            @Parameter(description = "현재 로그인한 회원 ID", required = true)
            @CurrentMember final Long memberId
    );
}

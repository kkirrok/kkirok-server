package com.kkirok.server.domain.member.api;

import com.kkirok.server.domain.member.application.dto.request.*;
import com.kkirok.server.domain.member.application.dto.response.AccessTokenGenerateResponse;
import com.kkirok.server.domain.member.application.dto.response.EmailVerificationStatusResponse;
import com.kkirok.server.domain.member.application.dto.response.FoundEmailResponse;
import com.kkirok.server.domain.member.application.dto.response.MemberLoginResponse;
import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.member.exception.MemberSuccessCode;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;
import com.kkirok.server.global.auth.jwt.exception.TokenErrorCode;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.common.redis.exception.RedisErrorCode;
import com.kkirok.server.global.external.r2.exception.R2ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Member API", description = "서비스 내 Member 관련 API")
public interface MemberApi {

    String REFRESH_TOKEN = "refreshToken";

    @Operation(
            summary = "소셜 로그인/회원가입",
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
    public ResponseEntity<SuccessResponse<MemberLoginResponse>> signUp(
            @Parameter(description = "소셜 인증 코드", required = true)
            @RequestParam final String authorizationCode,
            @RequestBody final MemberLoginRequest loginRequest,
            HttpServletResponse httpServletResponse
    );

    @Operation(
            summary = "이메일 인증번호 발송",
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
            summary = "이메일 인증번호 확인",
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
            summary = "이메일 찾기",
            description = """
                    이름, 생년월일, 전화번호가 일치하는 회원의 이메일을 조회합니다.

                    - 요청 바디: `name`, `birth`, `phone`
                    - 응답: 가입된 이메일
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "ACCOUNT_RECOVERY_INFO_MISMATCH")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "FIND_EMAIL_SUCCESS")
    ResponseEntity<SuccessResponse<FoundEmailResponse>> findEmail(
            @Valid @RequestBody final FindEmailRequest request
    );

    @Operation(
            summary = "비밀번호 재설정",
            description = """
                    이메일 인증이 완료된 이메일과 이름이 일치하면 로컬 계정 비밀번호를 재설정합니다.

                    - 요청 바디: `email`, `name`, `newPassword`
                    - 사전 조건: 이메일 인증 완료
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = EmailErrorCode.class, code = "EMAIL_NOT_VERIFIED"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_READ_FAILED"),
            @ApiErrorCodeExample(codeType = RedisErrorCode.class, code = "REDIS_DELETE_FAILED"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "ACCOUNT_RECOVERY_FORBIDDEN"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "ACCOUNT_RECOVERY_INFO_MISMATCH")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "RESET_PASSWORD_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> resetPassword(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember final Long memberId,
            @Valid @RequestBody final ResetPasswordRequest request
    );

    @Operation(
            summary = "로컬 회원가입",
            description = """
                    이메일/비밀번호 기반 로컬 회원가입을 수행합니다.
                    회원가입 성공 시 즉시 로그인 처리됩니다.

                    - 요청 바디: `email`, `nickname`, `password`
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
    public ResponseEntity<SuccessResponse<MemberLoginResponse>> localSignUp(
            @Valid @RequestBody final LocalSignUpRequest request,
            HttpServletResponse httpServletResponse
    );

    @Operation(
            summary = "로컬 로그인",
            description = """
                    이메일/비밀번호 기반 로컬 로그인을 수행합니다.

                    - 요청 바디: `email`, `password`
                    - 응답: accessToken + 사용자 정보
                    - refreshToken은 HttpOnly 쿠키로 내려갑니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "LOCAL_LOGIN_FAILED")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "LOCAL_LOGIN_SUCCESS")
    public ResponseEntity<SuccessResponse<MemberLoginResponse>> localLogin(
            @Valid @RequestBody final LocalLoginRequest request,
            HttpServletResponse httpServletResponse
    );

    @Operation(
            summary = "액세스 토큰 재발급",
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
    public ResponseEntity<SuccessResponse<AccessTokenGenerateResponse>> issueAccessTokenUsingRefreshToken(
            @Parameter(description = "리프레시 토큰 쿠키", required = true)
            @CookieValue(value = REFRESH_TOKEN) final String refreshToken
    );

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인 사용자의 refreshToken을 서버 저장소에서 삭제합니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = TokenErrorCode.class, code = "REFRESH_TOKEN_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "SIGN_OUT_SUCCESS")
    public ResponseEntity<SuccessResponse<Void>> signOut(
            @Parameter(description = "현재 로그인한 회원 ID", required = true)
            @CurrentMember final Long memberId
    );

    @Operation(
            summary = "프로필 설정(온보딩)",
            description = """
                    회원가입 이후 프로필 정보와 온보딩 정보를 함께 설정합니다.

                    요청 전체는 `multipart/form-data`로 전송하며, 텍스트 필드와 `profileImage` 파일 파트를 함께 보냅니다.
                    - 프로필 설정: `name`, `birth`, `phone`, `nickname`, `gender`
                    - 온보딩 정보: `purpose`, `habits`
                    - 선택값: `profileImage`, `purpose`, `habits`
                    - `profileImage`는 URL 문자열이 아니라 파일 파트로 업로드합니다.
                    - 업로드된 프로필 이미지는 R2에 저장되고, DB에는 이미지 URL이 아닌 저장 key가 보관됩니다.
                    - 식습관을 선택하지 않으면 빈 리스트로 저장됩니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "ONBOARDING_HABIT_MAX_COUNT"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = R2ErrorCode.class, code = "FILE_STREAM_READ_FAILED"),
            @ApiErrorCodeExample(codeType = R2ErrorCode.class, code = "FILE_UPLOAD_FAILED")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "PROFILE_SETTING_SUCCESS")
    public ResponseEntity<SuccessResponse<Void>> updateProfile(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @ModelAttribute ProfileSettingRequest request,
            @RequestPart(required = false) MultipartFile profileImage
    );




}

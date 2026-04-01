package com.kkirok.server.domain.member.api;

import com.kkirok.server.domain.member.application.dto.request.FindEmailRequest;
import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.member.application.dto.request.ResetPasswordRequest;
import com.kkirok.server.domain.member.application.dto.response.FoundEmailResponse;
import com.kkirok.server.domain.member.application.dto.response.OnboardingProfileResponse;
import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.member.exception.MemberSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.common.redis.exception.RedisErrorCode;
import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Member API", description = "서비스 내 Member 관련 API")
public interface MemberApi {

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

    @Operation(summary = "프로필 조회(온보딩)", description = """
            프로필 설정 전, 기본 정보를 조회합니다.
            멤버 기본 정보( 닉네임, 성별, 프로필사진 )와 목표•식습관 유형 정보(라벨, 선택여부)를 반환합니다.
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "MEMBER_NOT_FOUND")
    })
    ResponseEntity<OnboardingProfileResponse> getOnboardingProfile(
            @Parameter(hidden = true) @CurrentMember final Long memberId
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
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_FILE_STREAM_READ_FAILED"),
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_FILE_UPLOAD_FAILED")
    })
    @ApiSuccessCodeExample(codeType = MemberSuccessCode.class, code = "PROFILE_SETTING_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> updateProfile(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @ModelAttribute ProfileSettingRequest request,
            @RequestPart(required = false) MultipartFile profileImage
    );

    @Operation(summary = "회원 탈퇴", description = """
            회원탈퇴입니다. 복구 정책을 대비하여 완전 삭제하지 않습니다.
            """)
    ResponseEntity<SuccessResponse<Void>> quitMember(
            @Parameter(hidden = true) @CurrentMember Long memberId
    );

}

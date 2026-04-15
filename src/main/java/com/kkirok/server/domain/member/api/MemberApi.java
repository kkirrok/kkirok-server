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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Member API", description = "서비스 내 Member 관련 API")
public interface MemberApi {

    @Operation(
            summary = "이메일 찾기 []",
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
            summary = "비밀번호 재설정 [USER]",
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
            @CurrentMember final Long memberId,
            @Valid @RequestBody final ResetPasswordRequest request
    );

    @Operation(summary = "프로필 조회(온보딩) [USER]", description = """
            프로필 설정 전, 기본 정보를 조회합니다.
            멤버 기본 정보( 닉네임, 성별, 프로필사진 )와 목표•식습관 유형 정보(라벨, 선택여부)를 반환합니다.
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = MemberErrorCode.class, code = "MEMBER_NOT_FOUND")
    })
    ResponseEntity<OnboardingProfileResponse> getOnboardingProfile(
            @CurrentMember final Long memberId
    );

    @Operation(
            summary = "프로필 설정(온보딩) [USER, PENDING]",
            description = """
                    회원가입 이후 프로필 정보와 온보딩 정보를 함께 설정합니다.
                    
                    목표(purpose)는 하나(필수), 식습관 유형(habits)는 5개 이하이어야 합니다. 식습관 유형은 선택되지 않아도 됩니다.

                    프로필을 수정하는 경우, 프로필 조회 API에서 응답 받은 결과를 기준으로 호출해주세요.
                    예를 들어 닉네임만 바꾸고 싶다면 프로필 조회 API 응답값에서 닉네임만 변경해서 전송하면 됩니다.
                    프로필 이미지를 업로드하지 않으면 기존 이미지가 유지됩니다.
                    프로필 이미지를 기본 이미지로 바꾸고 싶다면 기본 이미지 파일을 업로드해주세요.

                    ## API 호출 방법

                    `multipart/form-data`로 요청합니다. `request` 파트는 JSON Blob으로 추가하고, `image` 파트는 선택값입니다.

                    | Part name | Required | Value |
                    | --- | --- | --- |
                    | `request` | O | `application/json` 타입의 JSON Blob |
                    | `image` | X | 이미지 파일 |

                    ### request JSON

                    ```json
                    {
                      "name": "김준용",
                      "birth": "2002-04-13",
                      "phone": "01012345678",
                      "nickname": "멋진닉네임",
                      "gender": "MALE",
                      "purpose": "LOSE_WEIGHT",
                      "habits": ["MEAT"]
                    }
                    ```

                    `Content-Type`은 직접 지정하지 않습니다. 브라우저가 boundary를 포함한 `multipart/form-data` 값을 자동으로 생성해야 합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ProfileSettingRequest.ProfileSettingMultipartRequest.class),
                            encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
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
            @CurrentMember Long memberId,
            @Valid @RequestPart(name = "request") ProfileSettingRequest request,
            @RequestPart(name = "image", required = false) MultipartFile profileImage
    );

    @Operation(summary = "회원 탈퇴 [USER]", description = """
            회원탈퇴입니다. 복구 정책을 대비하여 완전 삭제하지 않습니다.
            """)
    ResponseEntity<SuccessResponse<Void>> quitMember(
            @CurrentMember Long memberId
    );

}

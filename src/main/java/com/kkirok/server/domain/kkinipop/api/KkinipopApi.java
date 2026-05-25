package com.kkirok.server.domain.kkinipop.api;

import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopCustomEmojiCreateRequest;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopCustomEmojiResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopDailyPostResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopEmojiListResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopGroupResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMemberSummaryResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionDateResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMyKkirokStatusResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopPostResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopReactionSummaryResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopSystemEmojiResponse;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.domain.kkinipop.exception.KkinipopSuccessCode;
import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Kkinipop API", description = "끼니팝 관련 API")
public interface KkinipopApi {

    @Operation(
            summary = "내 그룹 목록 조회 [USER]",
            description = """
                    현재 사용자가 속한 끼니팝 그룹 목록을 조회합니다.
                    """
    )
    @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED")
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "GROUP_LIST_SUCCESS")
    ResponseEntity<SuccessResponse<List<KkinipopGroupResponse>>> getGroups(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "시스템 이모지 enum 조회 [USER]",
            description = """
                    프론트에서 사용할 시스템 이모지 enum 목록을 조회합니다.
                    
                    시스템 이모지 코드는 `SYSTEM_` 접두사를 사용합니다.
                    
                    """
    )
    @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED")
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "SYSTEM_EMOJI_LIST_SUCCESS")
    ResponseEntity<SuccessResponse<List<KkinipopSystemEmojiResponse>>> getSystemEmojis(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "그룹 멤버 목록 조회 [USER]",
            description = """
                    특정 그룹의 멤버 목록을 조회합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - 응답: 멤버 ID, 표시 이름, 프로필 이미지, 내 프로필 여부, 방장 여부
                    - 멤버 프로필 이미지는 r2에서 조회하고 사이즈 조정하여
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "GROUP_MEMBER_LIST_SUCCESS")
    ResponseEntity<SuccessResponse<List<KkinipopMemberSummaryResponse>>> getGroupMembers(
            @CurrentMember Long memberId,
            @Parameter(description = "조회할 그룹 ID", required = true, example = "10")
            Long groupId
    );

    @Operation(
            summary = "그룹 생성 [USER]",
            description = """
                    끼니팝 그룹을 생성합니다.

                    - 요청 바디: 그룹 이름
                    - 생성 결과: 방장으로 바로 가입되며, 초대 코드를 함께 반환합니다.
                    
                    """
    )
    @ApiResponse(responseCode = "201", useReturnTypeSchema = true)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "INVALID_GROUP_NAME")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "GROUP_CREATE_SUCCESS")
    ResponseEntity<SuccessResponse<KkinipopGroupResponse>> createGroup(
            @CurrentMember Long memberId,
            @Parameter(description = "그룹 생성 요청", required = true)
            @Valid @RequestBody KkinipopGroupCreateRequest request
    );

    @Operation(
            summary = "그룹 참여 [USER]",
            description = """
                    초대 코드로 그룹에 참여합니다.

                    - 요청 파라미터: 초대 코드
                    - 성공 시: 참여한 그룹 정보와 현재 인원 수를 반환합니다.
                    """
    )
    @ApiResponse(responseCode = "201", useReturnTypeSchema = true)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_JOIN_CONFLICT")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "GROUP_JOIN_SUCCESS")
    ResponseEntity<SuccessResponse<KkinipopGroupResponse>> joinGroup(
            @CurrentMember Long memberId,
            @Parameter(description = "그룹 초대 코드", required = true, example = "AB12CD")
            String code
    );

    @Operation(
            summary = "그룹 탈퇴 [USER]",
            description = """
                    현재 사용자가 그룹에서 탈퇴합니다.

                    - 일반 멤버: 그룹 소속만 종료됩니다.
                    - 방장: 그룹 전체가 삭제됩니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "GROUP_LEAVE_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> quitGroup(
            @CurrentMember Long memberId,
            @Parameter(description = "탈퇴할 그룹 ID", required = true, example = "10")
            Long groupId
    );

    @Operation(
            summary = "그룹 삭제 [USER]",
            description = """
                    방장이 그룹을 삭제합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹의 방장이어야 합니다.
                    - 결과: 그룹, 소속, 미션, 게시글, 커스텀 이모지가 함께 삭제됩니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MANAGEMENT_FORBIDDEN")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "GROUP_DELETE_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> deleteGroup(
            @CurrentMember Long memberId,
            @Parameter(description = "삭제할 그룹 ID", required = true, example = "10")
            Long groupId
    );

    @Operation(
            summary = "그룹 멤버 추방 [USER]",
            description = """
                    방장이 그룹 멤버를 추방합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹의 방장이어야 합니다.
                    - 제약: 방장은 추방할 수 없습니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MANAGEMENT_FORBIDDEN")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "GROUP_MEMBER_DELETE_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> removeGroupMember(
            @CurrentMember Long memberId,
            @Parameter(description = "멤버를 추방할 그룹 ID", required = true, example = "10")
            Long groupId,
            @Parameter(description = "추방할 대상 회원 ID", required = true, example = "3")
            Long memberIdToRemove
    );

    @Operation(
            summary = "게시글 조회 [USER]",
            description = """
                    이번 주 월요일부터 일요일까지 게시글을 날짜별로 묶어서 조회합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - 조회 범위: 오늘이 속한 주의 월요일부터 일요일까지 게시글을 조회합니다.
                    - `missionId`가 있으면 오늘 날짜 미션 중 해당 미션에 속한 게시글만 조회합니다.
                    - `missionId`가 없으면 미션 조건 없이 전체 게시글을 조회합니다.
                    - 응답: 날짜별 게시글 목록, 요일 라벨, 요일 숫자(0:일 ~ 6:토)
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "MISSION_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "POST_LIST_SUCCESS")
    ResponseEntity<SuccessResponse<List<KkinipopDailyPostResponse>>> getPosts(
            @CurrentMember Long memberId,
            @Parameter(description = "조회할 그룹 ID", required = true, example = "10")
            Long groupId,
            @Parameter(description = "오늘 날짜 미션 중 특정 미션 게시글만 조회할 때 사용하는 미션 ID. 없으면 전체 게시글을 조회합니다.", required = false, example = "7")
            Long missionId
    );

    @Operation(
            summary = "오늘 나의끼록과 같이 저장 가능 횟수 조회 [USER]",
            description = """
                    오늘 기준 나의끼록과 같이 저장 가능한 남은 횟수를 조회합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - `saveToPersonalLog=true`인 끼니팝 게시글은 나의끼록과 같이 저장됩니다.
                    - 하루 최대 횟수와 현재 남은 횟수를 함께 반환합니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "MY_KKIROK_STATUS_SUCCESS")
    ResponseEntity<SuccessResponse<KkinipopMyKkirokStatusResponse>> getMyKkirokStatus(
            @CurrentMember Long memberId,
            @Parameter(description = "조회할 그룹 ID", required = true, example = "10")
            Long groupId
    );

    @Operation(
            summary = "게시글 작성 [USER]",
            description = """
                    현재 시간에 해당하는 미션에 게시글을 작성합니다.

                    `multipart/form-data`로 요청합니다.
                    - `saveToPersonalLog`: 나의끼록과 같이 저장할지 여부
                    - `image`: 업로드할 이미지 파일
                    - 'scanType': 이미지 업로드 수단
                    - 현재 시간에 해당하는 실시간 미션이 없으면 실패합니다.
                    - `saveToPersonalLog=true`인 끼니팝 게시글은 하루 최대 3회까지 저장할 수 있습니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = KkinipopPostMultipartRequest.class)
                    )
            )
    )
    @ApiResponse(responseCode = "201", useReturnTypeSchema = true)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "LIVE_MISSION_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "LIVE_MISSION_ENDED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "INVALID_POST_REQUEST"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "PHOTO_REQUIRED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GENERAL_POST_LIMIT_EXCEEDED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "MISSION_POST_LIMIT_EXCEEDED"),
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_FILE_STREAM_READ_FAILED"),
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_FILE_UPLOAD_FAILED")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "POST_CREATE_SUCCESS")
    ResponseEntity<SuccessResponse<KkinipopPostResponse>> createPost(
            @CurrentMember Long memberId,
            @Parameter(description = "게시글을 작성할 그룹 ID", required = true, example = "10")
            Long groupId,
            @Parameter(description = "나의끼록과 같이 저장 여부", required = false, example = "false")
            @RequestParam("saveToPersonalLog") boolean saveToPersonalLog,
            @Parameter(description = "이미지 스캔타입", required = true, example = "CAMERA")
            @RequestPart("missionId") ScanType scanType,
            @Parameter(description = "업로드할 게시글 이미지 파일", required = true)
            @RequestPart("image") MultipartFile image
    );

    @Operation(
            summary = "게시글 삭제 [USER]",
            description = """
                    본인 게시글을 삭제합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - 제약: 본인 게시글만 삭제할 수 있습니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "POST_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_ACCESS_FORBIDDEN"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "POST_DELETE_FORBIDDEN")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "POST_DELETE_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> deletePost(
            @CurrentMember Long memberId,
            @Parameter(description = "게시글이 속한 그룹 ID", required = true, example = "10")
            Long groupId,
            @Parameter(description = "삭제할 게시글 ID", required = true, example = "55")
            Long postId
    );

    @Operation(
            summary = "이모지 조회 [USER]",
            description = """
                    그룹에서 사용할 시스템/커스텀 이모지 목록을 조회합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - 시스템 이모지는 image가 null입니다.
                    - 커스텀 이모지 코드는 `CUSTOM_{id}` 형식입니다.
                    - 시스템 이모지는 시스템 이모지 조회 api에서 종류를 조회할 수 있습니다.
                    
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "EMOJI_LIST_SUCCESS")
    ResponseEntity<SuccessResponse<KkinipopEmojiListResponse>> getEmojis(
            @CurrentMember Long memberId,
            @Parameter(description = "조회할 그룹 ID", required = true, example = "10")
            Long groupId
    );

    @Operation(
            summary = "커스텀 이모지 생성 [USER]",
            description = """
                    이미지 파일을 업로드해 커스텀 이모지를 생성합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - 제약: 그룹당 최대 3개까지 생성할 수 있습니다.
                    - 라벨: 업로드한 파일명에서 확장자를 제거한 값으로 생성됩니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = KkinipopCustomEmojiCreateRequest.KkinipopCustomEmojiMultipartRequest.class)
                    )
            )
    )
    @ApiResponse(responseCode = "201", useReturnTypeSchema = true)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "CUSTOM_EMOJI_LIMIT_EXCEEDED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "PHOTO_REQUIRED"),
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_FILE_STREAM_READ_FAILED"),
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_FILE_UPLOAD_FAILED")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "CUSTOM_EMOJI_CREATE_SUCCESS")
    ResponseEntity<SuccessResponse<KkinipopCustomEmojiResponse>> createCustomEmoji(
            @CurrentMember Long memberId,
            @Parameter(description = "커스텀 이모지를 생성할 그룹 ID", required = true, example = "10")
            Long groupId,
            @Parameter(description = "업로드할 커스텀 이모지 이미지 파일", required = true)
            @RequestPart("image") MultipartFile image
    );

    @Operation(
            summary = "커스텀 이모지 삭제 [USER]",
            description = """
                    커스텀 이모지를 삭제합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - 권한: 생성자 본인 또는 방장만 삭제할 수 있습니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "CUSTOM_EMOJI_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_ACCESS_FORBIDDEN"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "CUSTOM_EMOJI_DELETE_FORBIDDEN")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "CUSTOM_EMOJI_DELETE_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> deleteCustomEmoji(
            @CurrentMember Long memberId,
            @Parameter(description = "커스텀 이모지가 속한 그룹 ID", required = true, example = "10")
            Long groupId,
            @Parameter(description = "삭제할 커스텀 이모지 ID", required = true, example = "5")
            Long emojiId
    );

    @Operation(
            summary = "게시글 리액션 [USER]",
            description = """
                    게시글에 시스템/커스텀 이모지 리액션을 남기거나 해제합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - `emojiCode`는 `SYSTEM_...` 또는 `CUSTOM_{id}` 형식을 사용합니다.
                    - 시스템 이모지는 시스템 이모지 조회 api에서 종류를 조회할 수 있습니다.
                    - 같은 사용자가 같은 게시글에 같은 `emojiCode`로 다시 요청하면 기존 리액션을 삭제합니다.
                    - 응답의 `count`는 요청 처리 후 해당 이모지의 전체 리액션 수입니다.
                    - 응답의 `reacted`는 요청 처리 후 현재 사용자의 리액션 여부입니다.
                    """
    )
    @ApiResponse(responseCode = "201", useReturnTypeSchema = true)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "POST_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "CUSTOM_EMOJI_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_ACCESS_FORBIDDEN"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "INVALID_REACTION_REQUEST")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "REACTION_CREATE_SUCCESS")
    ResponseEntity<SuccessResponse<KkinipopReactionSummaryResponse>> reactToPost(
            @CurrentMember Long memberId,
            @Parameter(description = "게시글이 속한 그룹 ID", required = true, example = "10")
            Long groupId,
            @Parameter(description = "리액션을 남길 게시글 ID", required = true, example = "55")
            Long postId,
            @Parameter(description = "리액션 이모지 코드", required = true, example = "SYSTEM_HEART")
            @RequestParam("emojiCode") String emojiCode
    );

    @Operation(
            summary = "미션 조회 [USER]",
            description = """
                    오늘 날짜로 등록된 미션만 조회합니다.

                    - 사전 조건: 요청한 사용자가 해당 그룹에 속해 있어야 합니다.
                    - 모든 미션은 10분 동안 진행됩니다.
                    - 응답의 `missions`는 시작 시각이 오래된 순으로 정렬됩니다.
                    - 현재 시간에 해당하는 미션은 `isRealTime=true`로 표시됩니다.
                    - 현재 시간에 해당하는 미션이 없으면 조회할 수 없습니다.
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "GROUP_MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = KkinipopErrorCode.class, code = "INVALID_MISSION_REQUEST")
    })
    @ApiSuccessCodeExample(codeType = KkinipopSuccessCode.class, code = "MISSION_LIST_SUCCESS")
    ResponseEntity<SuccessResponse<KkinipopMissionDateResponse>> getTodayMissions(
            @CurrentMember Long memberId,
            @Parameter(description = "조회할 그룹 ID", required = true, example = "10")
            Long groupId
    );

    record KkinipopPostMultipartRequest(
            @Schema(description = "나의끼록과 같이 저장 여부", example = "false")
            Boolean saveToPersonalLog,
            @Schema(type = "string", format = "binary", description = "업로드할 게시글 이미지")
            MultipartFile image
    ) {
    }
}

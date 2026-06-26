package com.kkirok.server.domain.kkinipop.api;

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
import com.kkirok.server.domain.kkinipop.application.service.KkinipopEmojiService;
import com.kkirok.server.domain.kkinipop.application.service.KkinipopGroupService;
import com.kkirok.server.domain.kkinipop.application.service.KkinipopMissionService;
import com.kkirok.server.domain.kkinipop.application.service.KkinipopPostService;
import com.kkirok.server.domain.kkinipop.exception.KkinipopSuccessCode;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/kkinipop")
@RoleUserAuth
public class KkinipopController implements KkinipopApi {

    private final KkinipopGroupService kkinipopGroupService;
    private final KkinipopPostService kkinipopPostService;
    private final KkinipopMissionService kkinipopMissionService;
    private final KkinipopEmojiService kkinipopEmojiService;

    // 내가 속한 그룹 목록 조회 API
    @Override
    @GetMapping("/groups")
    public ResponseEntity<SuccessResponse<List<KkinipopGroupResponse>>> getGroups(@CurrentMember Long memberId) {
        return ResponseEntity.ok(SuccessResponse.of(KkinipopSuccessCode.GROUP_LIST_SUCCESS, kkinipopGroupService.getGroups(memberId)));
    }

    // 시스템 이모지 enum 조회 API
    @Override
    @GetMapping("/emojis/system")
    public ResponseEntity<SuccessResponse<List<KkinipopSystemEmojiResponse>>> getSystemEmojis(@CurrentMember Long memberId) {
        return ResponseEntity.ok(SuccessResponse.of(KkinipopSuccessCode.SYSTEM_EMOJI_LIST_SUCCESS, kkinipopEmojiService.getSystemEmojiOptions()));
    }

    // 그룹 내 멤버 목록 조회 API
    @Override
    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<SuccessResponse<List<KkinipopMemberSummaryResponse>>> getGroupMembers(
            @CurrentMember Long memberId,
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(KkinipopSuccessCode.GROUP_MEMBER_LIST_SUCCESS, kkinipopGroupService.getGroupMembers(memberId, groupId)));
    }

    // 그룹 생성 API
    @Override
    @PostMapping("/groups")
    public ResponseEntity<SuccessResponse<KkinipopGroupResponse>> createGroup(
            @CurrentMember Long memberId,
            @Valid @RequestBody KkinipopGroupCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(KkinipopSuccessCode.GROUP_CREATE_SUCCESS, kkinipopGroupService.createGroup(memberId, request)));
    }

    // 그룹 참여 API
    @Override
    @PostMapping("/groups/join")
    public ResponseEntity<SuccessResponse<KkinipopGroupResponse>> joinGroup(
            @CurrentMember Long memberId,
            @RequestParam String code
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(KkinipopSuccessCode.GROUP_JOIN_SUCCESS, kkinipopGroupService.joinGroup(memberId, code)));
    }

    // 그룹 탈퇴 API
    @Override
    @DeleteMapping("/groups/{groupId}/quit")
    public ResponseEntity<SuccessResponse<Void>> quitGroup(
            @CurrentMember Long memberId,
            @PathVariable Long groupId
    ) {
        kkinipopGroupService.leaveGroup(memberId, groupId);
        return ResponseEntity.ok(SuccessResponse.from(KkinipopSuccessCode.GROUP_LEAVE_SUCCESS));
    }

    // 그룹 삭제 API
    @Override
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<SuccessResponse<Void>> deleteGroup(
            @CurrentMember Long memberId,
            @PathVariable Long groupId
    ) {
        kkinipopGroupService.deleteGroup(memberId, groupId);
        return ResponseEntity.ok(SuccessResponse.from(KkinipopSuccessCode.GROUP_DELETE_SUCCESS));
    }

    // 그룹에서 추방 API
    @Override
    @DeleteMapping("/groups/{groupId}/members/{memberId}")
    public ResponseEntity<SuccessResponse<Void>> removeGroupMember(
            @CurrentMember Long memberId,
            @PathVariable Long groupId,
            @PathVariable("memberId") Long memberIdToRemove
    ) {
        kkinipopGroupService.removeGroupMember(memberId, groupId, memberIdToRemove);
        return ResponseEntity.ok(SuccessResponse.from(KkinipopSuccessCode.GROUP_MEMBER_DELETE_SUCCESS));
    }

    // 끼니팝 게시글 목록 조회 API
    @Override
    @GetMapping("/groups/{groupId}/posts")
    public ResponseEntity<SuccessResponse<List<KkinipopDailyPostResponse>>> getPosts(
            @CurrentMember Long memberId,
            @PathVariable Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long missionId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(KkinipopSuccessCode.POST_LIST_SUCCESS, kkinipopPostService.getPosts(memberId, groupId, date, missionId)));
    }

    // 오늘 나의끼록과 같이 저장 가능한 횟수 조회 API
    @Override
    @GetMapping("/groups/{groupId}/posts/my-kkirok/status")
    public ResponseEntity<SuccessResponse<KkinipopMyKkirokStatusResponse>> getMyKkirokStatus(
            @CurrentMember Long memberId,
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(
                SuccessResponse.of(KkinipopSuccessCode.MY_KKIROK_STATUS_SUCCESS, kkinipopPostService.getMyKkirokStatus(memberId, groupId))
        );
    }

    // 글 기록 API, 현재 시간에 해당하는 미션에 대한 기록으로 저장
    @Override
    @PostMapping("/groups/{groupId}/posts")
    public ResponseEntity<SuccessResponse<KkinipopPostResponse>> createPost(
            @CurrentMember Long memberId,
            @PathVariable Long groupId,
            @RequestParam(value = "saveToPersonalLog", required = false, defaultValue = "false") boolean saveToPersonalLog,
            @RequestParam("missionId") ScanType scanType,
            @RequestPart("image") MultipartFile image
            ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(KkinipopSuccessCode.POST_CREATE_SUCCESS, kkinipopPostService.createPost(memberId, groupId, saveToPersonalLog, image, scanType)));
    }

    // 글 삭제 API
    @Override
    @DeleteMapping("/groups/{groupId}/posts/{postId}")
    public ResponseEntity<SuccessResponse<Void>> deletePost(
            @CurrentMember Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long postId
    ) {
        kkinipopPostService.deletePost(memberId, groupId, postId);
        return ResponseEntity.ok(SuccessResponse.from(KkinipopSuccessCode.POST_DELETE_SUCCESS));
    }

    // 이모지 조회 API
    @Override
    @GetMapping("/groups/{groupId}/emojis")
    public ResponseEntity<SuccessResponse<KkinipopEmojiListResponse>> getEmojis(@CurrentMember Long memberId, @PathVariable Long groupId) {
        return ResponseEntity.ok(SuccessResponse.of(KkinipopSuccessCode.EMOJI_LIST_SUCCESS, kkinipopEmojiService.getEmojis(memberId, groupId)));
    }

    // 커스텀 이모지 생성 API
    @Override
    @PostMapping("/groups/{groupId}/emojis")
    public ResponseEntity<SuccessResponse<KkinipopCustomEmojiResponse>> createCustomEmoji(
            @CurrentMember Long memberId,
            @PathVariable Long groupId,
            @RequestPart("image") MultipartFile image
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(KkinipopSuccessCode.CUSTOM_EMOJI_CREATE_SUCCESS, kkinipopEmojiService.createCustomEmoji(memberId, groupId, image)));
    }

    // 커스텀 이모지 삭제 API
    @Override
    @DeleteMapping("/groups/{groupId}/emojis/{emojiId}")
    public ResponseEntity<SuccessResponse<Void>> deleteCustomEmoji(
            @CurrentMember Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long emojiId
    ) {
        kkinipopEmojiService.deleteCustomEmoji(memberId, groupId, emojiId);
        return ResponseEntity.ok(SuccessResponse.from(KkinipopSuccessCode.CUSTOM_EMOJI_DELETE_SUCCESS));
    }

    // 반응하기 API
    @Override
    @PostMapping("/groups/{groupId}/posts/{postId}/reaction")
    public ResponseEntity<SuccessResponse<KkinipopReactionSummaryResponse>> reactToPost(
            @CurrentMember Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestParam("emojiCode") String emojiCode
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(KkinipopSuccessCode.REACTION_CREATE_SUCCESS, kkinipopPostService.reactToPost(memberId, groupId, postId, emojiCode)));
    }

    // 오늘의 미션 조회 API
    @Override
    @GetMapping("/groups/{groupId}/missions")
    public ResponseEntity<SuccessResponse<KkinipopMissionDateResponse>> getTodayMissions(
            @CurrentMember Long memberId,
            @PathVariable Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(SuccessResponse.of(KkinipopSuccessCode.MISSION_LIST_SUCCESS, kkinipopMissionService.getTodayMissions(memberId, groupId, date)));
    }
}

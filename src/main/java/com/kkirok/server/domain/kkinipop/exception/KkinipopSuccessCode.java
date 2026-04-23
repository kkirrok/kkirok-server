package com.kkirok.server.domain.kkinipop.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KkinipopSuccessCode implements BaseSuccessCode {
    GROUP_LIST_SUCCESS(200, "끼니팝 그룹 목록 조회 성공"),
    GROUP_MEMBER_LIST_SUCCESS(200, "끼니팝 그룹 멤버 목록 조회 성공"),
    SYSTEM_EMOJI_LIST_SUCCESS(200, "끼니팝 시스템 이모지 목록 조회 성공"),
    GROUP_CREATE_SUCCESS(201, "끼니팝 그룹 생성 성공"),
    GROUP_JOIN_SUCCESS(201, "끼니팝 그룹 참여 성공"),
    GROUP_SWITCH_SUCCESS(200, "끼니팝 그룹 전환 성공"),
    GROUP_LEAVE_SUCCESS(200, "끼니팝 그룹 탈퇴 성공"),
    GROUP_DELETE_SUCCESS(200, "끼니팝 그룹 삭제 성공"),
    GROUP_MEMBER_DELETE_SUCCESS(200, "끼니팝 그룹 멤버 삭제 성공"),
    POST_LIST_SUCCESS(200, "끼니팝 게시글 목록 조회 성공"),
    MY_KKIROK_STATUS_SUCCESS(200, "나의끼록과 같이 저장 가능 횟수 조회 성공"),
    EMOJI_LIST_SUCCESS(200, "끼니팝 이모지 목록 조회 성공"),
    MISSION_LIST_SUCCESS(200, "오늘의 끼니팝 미션 조회 성공"),
    DASHBOARD_GET_SUCCESS(200, "끼니팝 홈 조회 성공"),
    MISSION_CREATE_SUCCESS(201, "끼니팝 미션 생성 성공"),
    ARCHIVE_GET_SUCCESS(200, "끼니팝 모아보기 조회 성공"),
    POST_CREATE_SUCCESS(201, "끼니팝 게시글 작성 성공"),
    POST_DELETE_SUCCESS(200, "끼니팝 게시글 삭제 성공"),
    REACTION_CREATE_SUCCESS(201, "끼니팝 리액션 등록 성공"),
    CUSTOM_EMOJI_CREATE_SUCCESS(201, "끼니팝 커스텀 이모지 생성 성공"),
    CUSTOM_EMOJI_DELETE_SUCCESS(200, "끼니팝 커스텀 이모지 삭제 성공");

    private final int status;
    private final String message;
}

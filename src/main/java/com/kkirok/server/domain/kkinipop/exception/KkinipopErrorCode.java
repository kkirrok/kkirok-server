package com.kkirok.server.domain.kkinipop.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KkinipopErrorCode implements BaseErrorCode {
    INVALID_GROUP_NAME(400, "그룹 이름이 올바르지 않습니다."),
    INVALID_INVITE_CODE(400, "초대코드가 올바르지 않습니다."),
    INVALID_DATE_RANGE(400, "1주일 이내의 날짜만 조회할 수 있습니다."),
    INVALID_MISSION_TIME(400, "실시간 미션은 10분 동안만 진행할 수 있습니다."),
    INVALID_MISSION_REQUEST(400, "미션 요청이 올바르지 않습니다."),
    INVALID_POST_REQUEST(400, "게시글 요청이 올바르지 않습니다."),
    LIVE_MISSION_NOT_FOUND(400, "현재 시간에 해당하는 실시간 미션이 없습니다."),
    LIVE_MISSION_ENDED(400, "현재 시간에 해당하는 실시간 미션이 종료되었습니다."),
    PHOTO_REQUIRED(400, "사진은 필수입니다."),
    INVALID_REACTION_REQUEST(400, "리액션 요청이 올바르지 않습니다."),
    MISSION_GENERATION_FAILED(500, "끼니팝 미션 생성에 실패했습니다."),

    GROUP_ACCESS_FORBIDDEN(403, "해당 그룹에 접근할 수 없습니다."),
    GROUP_MANAGEMENT_FORBIDDEN(403, "방장만 그룹을 관리할 수 있습니다."),
    GROUP_BANNED(403, "추방된 그룹에는 다시 참여할 수 없습니다."),
    POST_DELETE_FORBIDDEN(403, "본인 게시글만 삭제할 수 있습니다."),
    CUSTOM_EMOJI_DELETE_FORBIDDEN(403, "커스텀 이모지를 삭제할 권한이 없습니다."),

    GROUP_NOT_FOUND(404, "그룹을 찾을 수 없습니다."),
    GROUP_MEMBER_NOT_FOUND(404, "그룹 멤버를 찾을 수 없습니다."),
    MISSION_NOT_FOUND(404, "미션을 찾을 수 없습니다."),
    POST_NOT_FOUND(404, "게시글을 찾을 수 없습니다."),
    CUSTOM_EMOJI_NOT_FOUND(404, "커스텀 이모지를 찾을 수 없습니다."),

    GROUP_JOIN_CONFLICT(409, "이미 참여 중인 그룹입니다."),
    GENERAL_POST_LIMIT_EXCEEDED(409, "나의끼록과 같이 저장하는 끼니팝은 하루 3회까지 작성할 수 있습니다."),
    MISSION_POST_LIMIT_EXCEEDED(409, "해당 미션 기록은 오늘 더 이상 작성할 수 없습니다."),
    CUSTOM_EMOJI_LIMIT_EXCEEDED(409, "커스텀 이모지는 최대 2개까지 생성할 수 있습니다.")

    ;

    private final int status;
    private final String message;
}

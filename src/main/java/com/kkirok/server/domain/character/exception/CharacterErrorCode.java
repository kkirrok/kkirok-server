package com.kkirok.server.domain.character.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CharacterErrorCode implements BaseErrorCode {

    /*
        400 BadRequest
    */
    ITEM_INFO_REQUIRED(400, "아이템 정보가 필요합니다"),

    /*
        404 NotFound
     */
    CHARACTER_NOT_FOUND(404, "유저에게 해당되는 캐릭터가 존재하지 않습니다"),
    ITEM_NOT_FOUND(404, "존재하지 않는 item입니다"),

    /*
        422 Unprocessable Entity
     */
    ALREADY_WEARING(422, "이미 착용하고 있는 유형의 아이템입니다."),
    NOT_POSSESSING(422, "보유하고 있는 아이템이 아님.")

    ;

    private final int status;
    private final String message;

}

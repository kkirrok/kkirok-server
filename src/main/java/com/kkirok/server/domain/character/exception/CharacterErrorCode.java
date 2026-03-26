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

    /*
        404 NotFound
     */
    CHARACTER_NOT_FOUND(404, "유저에게 해당되는 캐릭터가 존재하지 않습니다")

    ;

    private final int status;
    private final String message;

}

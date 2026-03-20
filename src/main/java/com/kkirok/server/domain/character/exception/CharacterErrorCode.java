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

    ;

    private final int status;
    private final String message;

}

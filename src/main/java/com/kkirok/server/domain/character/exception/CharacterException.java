package com.kkirok.server.domain.character.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class CharacterException extends KkirokException {

    public CharacterException(CharacterErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}

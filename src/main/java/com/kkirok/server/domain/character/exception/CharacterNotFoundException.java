package com.kkirok.server.domain.character.exception;

public class CharacterNotFoundException extends CharacterException {

    public CharacterNotFoundException() {
        super(CharacterErrorCode.CHARACTER_NOT_FOUND);
    }
}

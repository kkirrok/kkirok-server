package com.kkirok.server.domain.character.exception;

public class ItemNotFoundException extends CharacterException {
    public ItemNotFoundException() {
        super(CharacterErrorCode.ITEM_NOT_FOUND);
    }
}

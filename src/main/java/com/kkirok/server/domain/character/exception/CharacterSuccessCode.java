package com.kkirok.server.domain.character.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CharacterSuccessCode implements BaseSuccessCode {

    /*
	    200 Ok
	*/
    CHARACTER_DEFAULT_INFO_GET_SUCCESS(200, "캐릭터 기본 정보 조회 성공"),
    POSSESSING_ITEMS_GET_SUCCESS(200, "보유 아이템 조회 성공"),
    ITEM_WEAR_SUCCESS(200, "아이템 장착 성공"),
    ITEM_UNWEAR_SUCCESS(200, "아이템 장착 해제 성공");

    private final int status;
    private final String message;

}

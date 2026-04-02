package com.kkirok.server.domain.character.api;

import com.kkirok.server.domain.character.application.dto.res.CharacterDefaultInfoResponse;
import com.kkirok.server.domain.character.application.dto.res.PossessingItemsResponse;
import com.kkirok.server.domain.character.exception.CharacterErrorCode;
import com.kkirok.server.domain.character.exception.CharacterSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Character API", description = "캐릭터 관련 API")
public interface CharacterApi {

    @Operation(
            summary = "캐릭터 기본 정보 조회 [USER]",
            description = """
                    현재 로그인한 사용자의 캐릭터 기본 정보를 조회합니다.

                    - 인증된 사용자 기준으로 조회합니다.
                    - 오늘 식사 기록을 함께 반영한 기본 상태 정보를 반환합니다.
                    - 응답: 캐릭터 기본 정보
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = CharacterErrorCode.class, code = "CHARACTER_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = CharacterSuccessCode.class, code = "CHARACTER_DEFAULT_INFO_GET_SUCCESS")
    ResponseEntity<SuccessResponse<CharacterDefaultInfoResponse>> characterDefaultInfo(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "보유 아이템 조회 [USER]",
            description = """
                    현재 로그인한 사용자가 보유한 아이템 목록을 조회합니다.

                    - 인증된 사용자 기준으로 조회합니다.
                    - 응답: 보유 아이템 / 미보유 아이템 목록
                    """
    )
    @ApiErrorCodeExamples({
    })
    @ApiSuccessCodeExample(codeType = CharacterSuccessCode.class, code = "POSSESSING_ITEMS_GET_SUCCESS")
    ResponseEntity<SuccessResponse<PossessingItemsResponse>> possessingItems(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "아이템 장착 [USER]",
            description = """
                    현재 로그인한 사용자의 캐릭터에 특정 아이템을 장착합니다.

                    - 인증된 사용자 기준으로 조회합니다.
                    - 경로 변수: `itemId`
                    - 같은 타입의 아이템을 이미 착용 중이면 장착할 수 없습니다.
                    - 응답: 없음
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = CharacterErrorCode.class, code = "ITEM_INFO_REQUIRED"),
            @ApiErrorCodeExample(codeType = CharacterErrorCode.class, code = "CHARACTER_NOT_FOUND"),
            @ApiErrorCodeExample(codeType = CharacterErrorCode.class, code = "NOT_POSSESSING"),
            @ApiErrorCodeExample(codeType = CharacterErrorCode.class, code = "ALREADY_WEARING_TYPE")
    })
    @ApiSuccessCodeExample(codeType = CharacterSuccessCode.class, code = "ITEM_WEAR_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> wearItem(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId,
            @Parameter(description = "장착할 아이템 ID", required = true)
            @PathVariable Long itemId
    );

    @Operation(
            summary = "아이템 해제 [USER]",
            description = """
                    현재 로그인한 사용자의 캐릭터에서 특정 아이템 장착을 해제합니다.

                    - 인증된 사용자 기준으로 조회합니다.
                    - 경로 변수: `itemId`
                    - 이미 해제된 아이템은 다시 해제할 수 없습니다.
                    - 응답: 없음
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = CharacterErrorCode.class, code = "ITEM_INFO_REQUIRED"),
            @ApiErrorCodeExample(codeType = CharacterErrorCode.class, code = "NOT_POSSESSING"),
            @ApiErrorCodeExample(codeType = CharacterErrorCode.class, code = "ALREADY_UNWEARING_ITEM")
    })
    @ApiSuccessCodeExample(codeType = CharacterSuccessCode.class, code = "ITEM_UNWEAR_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> unWearItem(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId,
            @Parameter(description = "장착 해제할 아이템 ID", required = true)
            @PathVariable Long itemId
    );



}

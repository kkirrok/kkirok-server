package com.kkirok.server.domain.character.api;

import com.kkirok.server.domain.character.application.dto.res.CharacterDefaultInfoResponse;
import com.kkirok.server.domain.character.application.dto.res.PossessingItemsResponse;
import com.kkirok.server.domain.character.application.service.CharacterInfoService;
import com.kkirok.server.domain.character.application.service.CharacterWearingService;
import com.kkirok.server.domain.character.exception.CharacterSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/character")
public class CharacterController implements CharacterApi {

    private final CharacterInfoService characterInfoService;
    private final CharacterWearingService characterWearingService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<CharacterDefaultInfoResponse>> characterDefaultInfo(@CurrentMember Long memberId) {
        CharacterDefaultInfoResponse characterInfo = characterInfoService.getCharacterInfo(memberId);
        return ResponseEntity.ok(SuccessResponse.of(CharacterSuccessCode.CHARACTER_DEFAULT_INFO_GET_SUCCESS, characterInfo));
    }

    @Override
    @GetMapping("/items")
    public ResponseEntity<SuccessResponse<PossessingItemsResponse>> possessingItems(@CurrentMember Long memberId) {
        PossessingItemsResponse itemInfo = characterInfoService.getItemInfo(memberId);
        return ResponseEntity.ok(SuccessResponse.of(CharacterSuccessCode.POSSESSING_ITEMS_GET_SUCCESS, itemInfo));
    }

    @Override
    @PatchMapping("/items/{itemId}/wear")
    public ResponseEntity<SuccessResponse<Void>> wearItem(@CurrentMember Long memberId, @PathVariable Long itemId) {
        characterWearingService.putOnItem(memberId, itemId);
        return ResponseEntity.ok(SuccessResponse.of(CharacterSuccessCode.ITEM_WEAR_SUCCESS, null));
    }

    @Override
    @PatchMapping("/items/{itemId}/wear")
    public ResponseEntity<SuccessResponse<Void>> unWearItem(@CurrentMember Long memberId, @PathVariable Long itemId) {
        characterWearingService.takeOffItem(memberId, itemId);
        return ResponseEntity.ok(SuccessResponse.of(CharacterSuccessCode.ITEM_UNWEAR_SUCCESS, null));
    }
}

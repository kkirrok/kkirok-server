package com.kkirok.server.domain.character.api;

import com.kkirok.server.domain.character.application.dto.res.CharacterDefaultInfoResponse;
import com.kkirok.server.domain.character.application.dto.res.PossessingItemsResponse;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/character")
public class CharacterController implements CharacterApi {

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<CharacterDefaultInfoResponse>> characterDefaultInfo(@CurrentMember Long memberId) {
        return null;
    }

    @Override
    @GetMapping("/items")
    public ResponseEntity<SuccessResponse<PossessingItemsResponse>> possessingItems(@CurrentMember Long memberId) {
        return null;
    }

    @Override
    @PostMapping("/items/{itemId}/wear")
    public ResponseEntity<SuccessResponse<Void>> wearItem(@PathVariable Long itemId) {
        return null;
    }

    @Override
    @DeleteMapping("/items/{itemId}/wear")
    public ResponseEntity<SuccessResponse<Void>> unWearItem(@PathVariable Long itemId) {
        return null;
    }
}

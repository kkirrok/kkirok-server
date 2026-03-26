package com.kkirok.server.domain.character.application.service;

import com.kkirok.server.domain.character.dao.CharacterRepository;
import com.kkirok.server.domain.character.domain.Character;
import com.kkirok.server.domain.character.domain.Item;
import com.kkirok.server.domain.character.domain.ItemPossession;
import com.kkirok.server.domain.character.exception.CharacterErrorCode;
import com.kkirok.server.domain.character.exception.CharacterException;
import com.kkirok.server.domain.character.exception.CharacterNotFoundException;
import com.kkirok.server.global.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterWearingService {

    private final CharacterRepository characterRepository;

    /*
        아이템 착용하기 메서드입니다

        기존 장착한 유형의 아이템일 경우 예외처리 됩니다.
        대체하는 경우를 대비하여 장착 여부를 반환하는 메서드( isItemWearing() )를 정의해놓았습니다
     */
    @Transactional
    public void putOnItem(Long memberId, Long itemId) {

        if (itemId == null) { // itemId null 체크
            throw new BadRequestException(CharacterErrorCode.ITEM_INFO_REQUIRED);
        }

        // 캐릭터 + 보유 아이템 정보 + 아이템 조회 ( fetch join )
        Character character = characterRepository.findCharacterFetchItems(memberId)
                .orElseThrow(CharacterNotFoundException::new);

        ItemPossession foundItemPossession = findPossessedItem(character.getItemPossessions(), itemId);
        Item requestedItem = foundItemPossession.getItem();

        // 이미 장착하고 있는 유형의 아이템이면 예외 발생
        boolean itemWearing = isItemWearing(character.getItemPossessions(), requestedItem);
        if (itemWearing) {
            throw new CharacterException(CharacterErrorCode.ALREADY_WEARING);
        }

        // 문제 없다면 아이템 장착
        foundItemPossession.wear();

    }

    // 현재 장착 아이템(attachedItems) 중 현재 착용하려는 아이템과 같은 타입의 아이템이 있는지
    // -> 타입 중복이어도 대체되는걸로 변경됐을 때를 대비해서 메서드로 분리함
    private boolean isItemWearing(List<ItemPossession> itemPossessions, Item requestedItem) {
        return itemPossessions.stream()
                .filter(ItemPossession::isWearing)
                .anyMatch(itemPossession -> itemPossession.hasSameItemType(requestedItem.getItemType()));
    }

    private ItemPossession findPossessedItem(List<ItemPossession> itemPossessions, Long itemId) {
        return itemPossessions.stream()
                .filter(itemPossession -> itemPossession.hasItemId(itemId))
                .findFirst()
                .orElseThrow(() -> new CharacterException(CharacterErrorCode.NOT_POSSESSING));
    }

}

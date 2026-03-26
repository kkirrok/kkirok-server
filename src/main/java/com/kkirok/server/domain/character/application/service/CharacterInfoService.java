package com.kkirok.server.domain.character.application.service;

import com.kkirok.server.domain.character.application.dto.res.CharacterDefaultInfoResponse;
import com.kkirok.server.domain.character.application.dto.res.ItemInfo;
import com.kkirok.server.domain.character.application.dto.res.PossessingItemsResponse;
import com.kkirok.server.domain.character.dao.CharacterRepository;
import com.kkirok.server.domain.character.dao.ItemRepository;
import com.kkirok.server.domain.character.dao.ItemWithPossessionProjection;
import com.kkirok.server.domain.character.domain.Character;
import com.kkirok.server.domain.character.exception.CharacterNotFoundException;
import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.domain.MealRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterInfoService {

    private final CharacterRepository characterRepository;
    private final ItemRepository itemRepository;
    private final MealRecordUseCase mealRecordUseCase;

    // 캐릭터 기본 정보 조회
    @Transactional(readOnly = true)
    public CharacterDefaultInfoResponse getCharacterInfo(Long memberId){

        // 멤버에 해당하는 캐릭터 조회
        Character character = characterRepository.findDefaultInfo(memberId)
                .orElseThrow(CharacterNotFoundException::new);

        // 오늘의 영양정보 조회
        List<MealRecord> todayMealRecords = mealRecordUseCase.getTodayRecords(memberId);

        return CharacterDefaultInfoResponse.create(character, todayMealRecords);

    }

    // 보유 & 미보유 아이템 조회
    @Transactional(readOnly = true)
    public PossessingItemsResponse getItemInfo(Long memberId){

        // 아이템 조회
        List<ItemWithPossessionProjection> items = itemRepository.findItemsWithPossessions(memberId);

        // 보유 아이템
        List<ItemInfo> possession = items.stream()
                .filter(ItemWithPossessionProjection::getPossession)
                .map( ItemInfo::fromProjection )
                .toList();

        // 미보유 아이템
        List<ItemInfo> notPossession = items.stream()
                .filter(item -> !item.getPossession())
                .map( ItemInfo::fromProjection )
                .toList();

        return PossessingItemsResponse.create(possession, notPossession);
    }

}

package com.kkirok.server.domain.character.application.service;

import com.kkirok.server.domain.character.dao.CharacterRepository;
import com.kkirok.server.domain.character.domain.Character;
import com.kkirok.server.domain.character.domain.CharacterStatusType;
import com.kkirok.server.domain.character.domain.CharacterType;
import com.kkirok.server.domain.character.domain.Item;
import com.kkirok.server.domain.character.domain.ItemPossession;
import com.kkirok.server.domain.character.domain.ItemType;
import com.kkirok.server.domain.character.exception.CharacterErrorCode;
import com.kkirok.server.domain.character.exception.CharacterException;
import com.kkirok.server.domain.character.exception.CharacterNotFoundException;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CharacterWearingServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @InjectMocks
    private CharacterWearingService characterWearingService;

    @Test
    @DisplayName("장착할 아이템 ID가 없으면 예외가 발생한다")
    void shouldThrowBadRequestException_whenItemIdIsNull() {
        assertThatThrownBy(() -> characterWearingService.putOnItem(1L, null))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.ITEM_INFO_REQUIRED);
    }

    @Test
    @DisplayName("회원의 캐릭터가 없으면 아이템을 장착할 수 없다")
    void shouldThrowCharacterNotFoundException_whenCharacterDoesNotExist() {
        Long memberId = 1L;
        Long itemId = 10L;
        given(characterRepository.findCharacterFetchItems(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> characterWearingService.putOnItem(memberId, itemId))
                .isInstanceOf(CharacterNotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.CHARACTER_NOT_FOUND);
    }

    @Test
    @DisplayName("보유하지 않은 아이템이면 예외가 발생한다")
    void shouldThrowCharacterException_whenCharacterDoesNotPossessItem() {
        Long memberId = 1L;
        Long itemId = 10L;
        Character character = createCharacterWithItemPossession(20L, ItemType.HAT, false);
        given(characterRepository.findCharacterFetchItems(memberId)).willReturn(Optional.of(character));

        assertThatThrownBy(() -> characterWearingService.putOnItem(memberId, itemId))
                .isInstanceOf(CharacterException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.NOT_POSSESSING);
    }

    @Test
    @DisplayName("보유한 아이템이면 장착 상태로 변경된다")
    void shouldWearItem_whenCharacterPossessesItem() {
        Long memberId = 1L;
        Long itemId = 10L;
        Character character = createCharacterWithItemPossession(itemId, ItemType.HAT, false);
        given(characterRepository.findCharacterFetchItems(memberId)).willReturn(Optional.of(character));

        characterWearingService.putOnItem(memberId, itemId);

        ItemPossession itemPossession = character.getItemPossessions().get(0);
        assertThat(itemPossession.getIsWearing()).isTrue();
        then(characterRepository).should().findCharacterFetchItems(memberId);
    }

    @Test
    @DisplayName("같은 타입의 아이템을 이미 장착 중이면 예외가 발생한다")
    void shouldThrowCharacterException_whenSameTypeItemIsAlreadyWorn() {
        Long memberId = 1L;
        Long itemId = 20L;
        Character character = createCharacterWithItemPossessions(
                createItemFixture(10L, ItemType.HAT, true),
                createItemFixture(itemId, ItemType.HAT, false)
        );
        given(characterRepository.findCharacterFetchItems(memberId)).willReturn(Optional.of(character));

        assertThatThrownBy(() -> characterWearingService.putOnItem(memberId, itemId))
                .isInstanceOf(CharacterException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.ALREADY_WEARING_TYPE);
    }

    @Test
    @DisplayName("해제할 아이템 ID가 없으면 예외가 발생한다")
    void shouldThrowBadRequestException_whenTakeOffItemIdIsNull() {
        assertThatThrownBy(() -> characterWearingService.takeOffItem(1L, null))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.ITEM_INFO_REQUIRED);
    }

    @Test
    @DisplayName("보유하지 않은 아이템은 장착 해제할 수 없다")
    void shouldThrowCharacterException_whenTakingOffNotPossessingItem() {
        Long memberId = 1L;
        Long itemId = 10L;
        given(characterRepository.findItemPossessionFetchItems(memberId, itemId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> characterWearingService.takeOffItem(memberId, itemId))
                .isInstanceOf(CharacterException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.NOT_POSSESSING);
    }

    @Test
    @DisplayName("이미 장착 해제된 아이템이면 예외가 발생한다")
    void shouldThrowCharacterException_whenItemIsAlreadyUnworn() {
        Long memberId = 1L;
        Long itemId = 10L;
        ItemPossession itemPossession = createItemPossession(createCharacter(), itemId, ItemType.HAT, false);
        given(characterRepository.findItemPossessionFetchItems(memberId, itemId))
                .willReturn(Optional.of(itemPossession));

        assertThatThrownBy(() -> characterWearingService.takeOffItem(memberId, itemId))
                .isInstanceOf(CharacterException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.ALREADY_UNWEARING_ITEM);
    }

    @Test
    @DisplayName("장착 중인 아이템은 장착 해제된다")
    void shouldTakeOffItem_whenItemIsWearing() {
        Long memberId = 1L;
        Long itemId = 10L;
        ItemPossession itemPossession = createItemPossession(createCharacter(), itemId, ItemType.HAT, true);
        given(characterRepository.findItemPossessionFetchItems(memberId, itemId))
                .willReturn(Optional.of(itemPossession));

        characterWearingService.takeOffItem(memberId, itemId);

        assertThat(itemPossession.getIsWearing()).isFalse();
        then(characterRepository).should().findItemPossessionFetchItems(memberId, itemId);
    }

    private Character createCharacterWithItemPossession(Long itemId, ItemType itemType, boolean isWearing) {
        return createCharacterWithItemPossessions(createItemFixture(itemId, itemType, isWearing));
    }

    private Character createCharacter() {
        CharacterType characterType = CharacterType.create("BASIC", "기본 캐릭터", "character-base.png");
        Character character = Character.builder()
                .member(MemberFixture.createLocalMember())
                .characterType(characterType)
                .name("뀨뀨뀨")
                .currentStatus(CharacterStatusType.NORMAL)
                .build();
        ReflectionTestUtils.setField(character, "id", 1L);
        return character;
    }

    private Character createCharacterWithItemPossessions(ItemFixture... itemFixtures) {
        Character character = createCharacter();

        List<ItemPossession> itemPossessions = List.of(itemFixtures).stream()
                .map(itemFixture -> createItemPossession(character, itemFixture.itemId(), itemFixture.itemType(), itemFixture.isWearing()))
                .toList();
        ReflectionTestUtils.setField(character, "itemPossessions", itemPossessions);
        return character;
    }

    private ItemPossession createItemPossession(Character character, Long itemId, ItemType itemType, boolean isWearing) {
        Item item = Item.builder()
                .name("아이템")
                .itemType(itemType)
                .itemImage("item.png")
                .build();
        ReflectionTestUtils.setField(item, "id", itemId);

        ItemPossession itemPossession = ItemPossession.builder()
                .character(character)
                .item(item)
                .build();
        ReflectionTestUtils.setField(itemPossession, "isWearing", isWearing);
        return itemPossession;
    }

    private ItemFixture createItemFixture(Long itemId, ItemType itemType, boolean isWearing) {
        return new ItemFixture(itemId, itemType, isWearing);
    }

    private record ItemFixture(Long itemId, ItemType itemType, boolean isWearing) {
    }
}

package com.kkirok.server.domain.character.application.service;

import com.kkirok.server.domain.character.dao.CharacterRepository;
import com.kkirok.server.domain.character.dao.CharacterTypeRepository;
import com.kkirok.server.domain.character.domain.Character;
import com.kkirok.server.domain.character.domain.CharacterType;
import com.kkirok.server.domain.character.exception.CharacterErrorCode;
import com.kkirok.server.domain.character.exception.CharacterException;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CharacterInitializationServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private CharacterTypeRepository characterTypeRepository;

    @InjectMocks
    private CharacterInitializationService characterInitializationService;

    @Test
    @DisplayName("회원가입이 완료되면 기본 캐릭터 하나를 생성한다")
    void shouldCreateInitialCharacter_whenMemberRegisters() {
        // Given
        Member member = MemberFixture.createLocalMember();
        CharacterType characterType = CharacterType.create("BASIC", "기본 캐릭터", "character-base.png");

        given(characterTypeRepository.findByTypeCode("BASIC")).willReturn(Optional.of(characterType));

        // When
        characterInitializationService.createInitialCharacter(member);

        // Then
        ArgumentCaptor<Character> characterCaptor = ArgumentCaptor.forClass(Character.class);
        then(characterRepository).should().save(characterCaptor.capture());

        Character character = characterCaptor.getValue();
        assertThat(character.getMember()).isSameAs(member);
        assertThat(character.getCharacterType()).isSameAs(characterType);
        assertThat(character.getName()).isEqualTo("기본 캐릭터");
        assertThat(character.getLevel()).isEqualTo(1);
        assertThat(character.getExp()).isEqualTo(0);
    }

    @Test
    @DisplayName("기본 캐릭터 타입이 없으면 초기 캐릭터를 생성할 수 없다")
    void shouldThrowCharacterException_whenDefaultCharacterTypeDoesNotExist() {
        // Given
        Member member = MemberFixture.createLocalMember();
        given(characterTypeRepository.findByTypeCode("BASIC")).willReturn(Optional.empty());

        // When, Then
        assertThatThrownBy(() -> characterInitializationService.createInitialCharacter(member))
                .isInstanceOf(CharacterException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.CHARACTER_TYPE_NOT_FOUND);
    }
}

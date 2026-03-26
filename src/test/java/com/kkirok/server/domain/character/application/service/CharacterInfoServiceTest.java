package com.kkirok.server.domain.character.application.service;

import com.kkirok.server.domain.character.application.dto.res.CharacterDefaultInfoResponse;
import com.kkirok.server.domain.character.dao.CharacterRepository;
import com.kkirok.server.domain.character.domain.Character;
import com.kkirok.server.domain.character.domain.CharacterStatusType;
import com.kkirok.server.domain.character.domain.CharacterType;
import com.kkirok.server.domain.character.exception.CharacterErrorCode;
import com.kkirok.server.domain.character.exception.CharacterNotFoundException;
import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.meal.domain.MealAiAnalysis;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CharacterInfoServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private MealRecordRepository mealRecordRepository;

    @InjectMocks
    private CharacterInfoService characterInfoService;

    @Test
    @DisplayName("회원의 캐릭터와 오늘 식사 기록이 존재하면 캐릭터 기본 정보를 반환한다")
    void shouldReturnCharacterDefaultInfo_whenCharacterAndTodayMealRecordsExist() {
        // Given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();
        Character character = createCharacter();
        MealRecord breakfast = createMealRecord(
                500,
                40L,
                30L,
                10L,
                8L,
                600L
        );
        MealRecord lunch = createMealRecord(
                700,
                60L,
                50L,
                20L,
                12L,
                900L
        );

        given(characterRepository.findDefaultInfo(memberId)).willReturn(Optional.of(character));
        given(mealRecordRepository.getSpecifiedDateMealRecords(
                eq(memberId),
                eq(today.atStartOfDay()),
                eq(today.plusDays(1).atStartOfDay())
        )).willReturn(List.of(breakfast, lunch));

        // When
        CharacterDefaultInfoResponse response = characterInfoService.getCharacterInfo(memberId);

        // Then
        assertThat(response).isEqualTo(new CharacterDefaultInfoResponse(
                10L,
                4,
                "뀨뀨뀨",
                120,
                100,
                "character-base.png",
                600,
                700,
                50,
                40,
                15,
                10,
                750
        ));

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        then(characterRepository).should().findDefaultInfo(memberId);
        then(mealRecordRepository).should()
                .getSpecifiedDateMealRecords(eq(memberId), startCaptor.capture(), endCaptor.capture());
        assertThat(startCaptor.getValue()).isEqualTo(today.atStartOfDay());
        assertThat(endCaptor.getValue()).isEqualTo(today.plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("회원의 캐릭터가 없으면 기본 정보를 조회할 수 없다")
    void shouldThrowCharacterNotFoundException_whenCharacterDoesNotExist() {
        // Given
        Long memberId = 1L;
        given(characterRepository.findDefaultInfo(memberId)).willReturn(Optional.empty());

        // When, Then
        assertThatThrownBy(() -> characterInfoService.getCharacterInfo(memberId))
                .isInstanceOf(CharacterNotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(CharacterErrorCode.CHARACTER_NOT_FOUND);
    }

    private Character createCharacter() {
        CharacterType characterType = CharacterType.create("BASIC", "기본 캐릭터", "character-base.png");
        Character character = Character.builder()
                .member(MemberFixture.createLocalMember())
                .characterType(characterType)
                .name("뀨뀨뀨")
                .currentStatus(CharacterStatusType.NORMAL)
                .build();
        ReflectionTestUtils.setField(character, "id", 10L);
        ReflectionTestUtils.setField(character, "level", 4);
        ReflectionTestUtils.setField(character, "exp", 120);
        return character;
    }

    private MealRecord createMealRecord(
            int kcal,
            long carbohydrateG,
            long proteinG,
            long fatG,
            long sugarG,
            long sodiumMg
    ) {
        MealRecord mealRecord = MealRecord.builder().build();
        MealAiAnalysis mealAiAnalysis = MealAiAnalysis.builder()
                .mealRecord(mealRecord)
                .detectedFoodName("food")
                .foodCategory("category")
                .nutritionSummary("summary")
                .kcal(kcal)
                .carbohydrateG(carbohydrateG)
                .proteinG(proteinG)
                .fatG(fatG)
                .sugarG(sugarG)
                .sodiumMg(sodiumMg)
                .build();
        ReflectionTestUtils.setField(mealRecord, "mealAiAnalyses", List.of(mealAiAnalysis));
        return mealRecord;
    }
}

package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionGenerateResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionGenerateResponse.MissionCandidate;
import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMissionPolicy;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.global.common.exception.InternalServerException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.global.external.openai.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class KkinipopMissionGenerateServiceTest {

    @Mock
    private KkinipopUseCase kkinipopUseCase;

    @Mock
    private KkinipopMissionInsertService kkinipopMissionInsertService;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private KkinipopMissionRepository missionRepository;

    @Test
    @DisplayName("다음날 미션을 생성하면 각 그룹에 공통 후보 중 5개를 랜덤 배정한다")
    void shouldGenerateNextDayMissionsForAllGroups() {
        KkinipopMissionGenerateService missionGenerateService =
                new KkinipopMissionGenerateService(kkinipopMissionInsertService, dateTimeProvider, openAiService, kkinipopUseCase, missionRepository);
        LocalDate today = LocalDate.of(2026, 4, 24);
        LocalDate targetDate = today.plusDays(1);
        KkinipopGroup firstGroup = createGroup(10L, "아침 챌린저스");
        KkinipopGroup secondGroup = createGroup(11L, "점심 챌린저스");
        ArgumentCaptor<List<KkinipopMission>> missionsCaptor = ArgumentCaptor.forClass(List.class);

        given(dateTimeProvider.today()).willReturn(today);
        given(kkinipopUseCase.findAllGroup()).willReturn(List.of(firstGroup, secondGroup));
        given(openAiService.createObjectResponse(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(KkinipopMissionGenerateResponse.class)
        )).willReturn(new KkinipopMissionGenerateResponse(createMissionCandidates()));

        missionGenerateService.generateNextDayMissions();

        then(kkinipopMissionInsertService).should(times(2)).insertMissions(missionsCaptor.capture());

        List<List<KkinipopMission>> savedMissionGroups = missionsCaptor.getAllValues();
        assertThat(savedMissionGroups).hasSize(2);
        assertThat(savedMissionGroups.get(0)).hasSize(KkinipopMissionPolicy.DAILY_MISSION_COUNT);
        assertThat(savedMissionGroups.get(1)).hasSize(KkinipopMissionPolicy.DAILY_MISSION_COUNT);
        assertThat(savedMissionGroups.get(0)).allSatisfy(mission -> {
            assertThat(mission.getStartAt().toLocalDate()).isEqualTo(targetDate);
            assertThat(mission.getStartAt().getMinute() % KkinipopMissionPolicy.REALTIME_SLOT_INTERVAL_MINUTES).isEqualTo(0);
            assertThat(mission.getDurationMinutes()).isEqualTo(KkinipopMissionPolicy.REALTIME_DURATION_MINUTES);
        });
    }

    @Test
    @DisplayName("다음날 미션 존재 여부와 무관하게 전체 그룹에 미션을 생성한다")
    void shouldGenerateMissionsWithoutCheckingExistingAssignments() {
        KkinipopMissionGenerateService missionGenerateService =
                new KkinipopMissionGenerateService(kkinipopMissionInsertService, dateTimeProvider, openAiService, kkinipopUseCase, missionRepository);
        LocalDate today = LocalDate.of(2026, 4, 24);
        LocalDate targetDate = today.plusDays(1);
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");

        given(dateTimeProvider.today()).willReturn(today);
        given(kkinipopUseCase.findAllGroup()).willReturn(List.of(group));
        given(openAiService.createObjectResponse(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(KkinipopMissionGenerateResponse.class)
        )).willReturn(new KkinipopMissionGenerateResponse(createMissionCandidates()));

        missionGenerateService.generateNextDayMissions();

        then(kkinipopMissionInsertService).should().insertMissions(anyList());
    }

    @Test
    @DisplayName("OpenAI 공통 후보 응답이 비정상이면 미션 생성 예외가 발생한다")
    void shouldThrowInternalServerException_whenOpenAiResponseIsInvalid() {
        KkinipopMissionGenerateService missionGenerateService =
                new KkinipopMissionGenerateService(kkinipopMissionInsertService, dateTimeProvider, openAiService, kkinipopUseCase, missionRepository);
        LocalDate today = LocalDate.of(2026, 4, 24);
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");

        given(dateTimeProvider.today()).willReturn(today);
        given(kkinipopUseCase.findAllGroup()).willReturn(List.of(group));
        given(openAiService.createObjectResponse(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(KkinipopMissionGenerateResponse.class)
        )).willReturn(null);

        assertThatThrownBy(missionGenerateService::generateNextDayMissions)
                .isInstanceOf(InternalServerException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.MISSION_GENERATION_FAILED);
    }

    @Test
    @DisplayName("SNAKE_CASE ObjectMapper 환경에서도 미션 응답의 startTime과 durationMinutes를 읽는다")
    void shouldDeserializeMissionCandidatesWithSnakeCaseObjectMapper() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        String json = """
                {
                  "missions": [
                    {
                      "title": "아침 식사 한 컷",
                      "startTime": "07:00",
                      "durationMinutes": 10
                    }
                  ]
                }
                """;

        KkinipopMissionGenerateResponse response = objectMapper.readValue(json, KkinipopMissionGenerateResponse.class);

        assertThat(response.missions()).hasSize(1);
        assertThat(response.missions().get(0).startTime()).isEqualTo(LocalTime.of(7, 0));
        assertThat(response.missions().get(0).durationMinutes()).isEqualTo(10);
    }

    @Test
    @DisplayName("OpenAI 응답이 재시도 중 성공하면 폴백을 호출하지 않는다")
    void shouldNotUseFallback_whenOpenAiSucceedsDuringRetry() {
        KkinipopMissionGenerateService missionGenerateService =
                new KkinipopMissionGenerateService(kkinipopMissionInsertService, dateTimeProvider, openAiService, kkinipopUseCase, missionRepository);
        LocalDate today = LocalDate.of(2026, 4, 24);
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");

        given(dateTimeProvider.today()).willReturn(today);
        given(kkinipopUseCase.findAllGroup()).willReturn(List.of(group));
        given(openAiService.createObjectResponse(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(KkinipopMissionGenerateResponse.class)
        )).willReturn(null, null, new KkinipopMissionGenerateResponse(createMissionCandidates()));

        missionGenerateService.generateNextDayMissions();

        then(missionRepository).shouldHaveNoInteractions();
        then(kkinipopMissionInsertService).should().insertMissions(anyList());
    }

    @Test
    @DisplayName("OpenAI 5번 모두 실패하면 최근 14일 미션에서 20개를 랜덤 선택해 폴백 풀로 저장한다")
    void shouldUseFallbackMissionPool_whenOpenAiFailsAllAttempts() {
        KkinipopMissionGenerateService missionGenerateService =
                new KkinipopMissionGenerateService(kkinipopMissionInsertService, dateTimeProvider, openAiService, kkinipopUseCase, missionRepository);
        LocalDate today = LocalDate.of(2026, 4, 24);
        LocalDate targetDate = today.plusDays(1);
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");
        ArgumentCaptor<List<KkinipopMission>> missionsCaptor = ArgumentCaptor.forClass(List.class);

        given(dateTimeProvider.today()).willReturn(today);
        given(kkinipopUseCase.findAllGroup()).willReturn(List.of(group));
        given(openAiService.createObjectResponse(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(KkinipopMissionGenerateResponse.class)
        )).willReturn(null);
        given(missionRepository.findMissionsBetween(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        )).willReturn(createRecentMissions(group));

        missionGenerateService.generateNextDayMissions();

        then(openAiService).should(times(5)).createObjectResponse(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(KkinipopMissionGenerateResponse.class)
        );
        then(kkinipopMissionInsertService).should().insertMissions(missionsCaptor.capture());
        assertThat(missionsCaptor.getValue()).hasSize(KkinipopMissionPolicy.DAILY_MISSION_COUNT);
        assertThat(missionsCaptor.getValue()).allSatisfy(mission ->
                assertThat(mission.getStartAt().toLocalDate()).isEqualTo(targetDate));
    }

    @Test
    @DisplayName("폴백 후보가 20개 미만이면 미션 생성 예외가 발생하고 저장하지 않는다")
    void shouldThrowInternalServerException_whenFallbackCandidatesAreInsufficient() {
        KkinipopMissionGenerateService missionGenerateService =
                new KkinipopMissionGenerateService(kkinipopMissionInsertService, dateTimeProvider, openAiService, kkinipopUseCase, missionRepository);
        LocalDate today = LocalDate.of(2026, 4, 24);
        KkinipopGroup group = createGroup(10L, "아침 챌린저스");

        given(dateTimeProvider.today()).willReturn(today);
        given(kkinipopUseCase.findAllGroup()).willReturn(List.of(group));
        given(openAiService.createObjectResponse(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(KkinipopMissionGenerateResponse.class)
        )).willReturn(null);
        given(missionRepository.findMissionsBetween(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        )).willReturn(createRecentMissions(group).subList(0, 10));

        assertThatThrownBy(missionGenerateService::generateNextDayMissions)
                .isInstanceOf(InternalServerException.class)
                .extracting("baseErrorCode")
                .isEqualTo(KkinipopErrorCode.MISSION_GENERATION_FAILED);

        then(kkinipopMissionInsertService).shouldHaveNoInteractions();
    }

    private KkinipopGroup createGroup(Long groupId, String groupName) {
        KkinipopGroup group = KkinipopGroup.create(new KkinipopGroupCreateRequest(groupName), "AB12CD");
        ReflectionTestUtils.setField(group, "id", groupId);
        return group;
    }

    private List<KkinipopMission> createRecentMissions(KkinipopGroup group) {
        List<KkinipopMission> missions = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            LocalDateTime startAt = LocalDate.of(2026, 4, 20).atTime(i / 2, (i % 2) * 30);
            missions.add(KkinipopMission.create(group, "fallback미션" + i, startAt, startAt.plusMinutes(10)));
        }
        return missions;
    }

    private List<MissionCandidate> createMissionCandidates() {
        return List.of(
                new MissionCandidate("미션1", LocalTime.of(8, 0), 10),
                new MissionCandidate("미션2", LocalTime.of(8, 30), 10),
                new MissionCandidate("미션3", LocalTime.of(9, 0), 10),
                new MissionCandidate("미션4", LocalTime.of(9, 30), 10),
                new MissionCandidate("미션5", LocalTime.of(10, 0), 10),
                new MissionCandidate("미션6", LocalTime.of(10, 30), 10),
                new MissionCandidate("미션7", LocalTime.of(11, 0), 10),
                new MissionCandidate("미션8", LocalTime.of(11, 30), 10),
                new MissionCandidate("미션9", LocalTime.of(12, 0), 10),
                new MissionCandidate("미션10", LocalTime.of(12, 30), 10),
                new MissionCandidate("미션11", LocalTime.of(13, 0), 10),
                new MissionCandidate("미션12", LocalTime.of(13, 30), 10),
                new MissionCandidate("미션13", LocalTime.of(14, 0), 10),
                new MissionCandidate("미션14", LocalTime.of(14, 30), 10),
                new MissionCandidate("미션15", LocalTime.of(15, 0), 10),
                new MissionCandidate("미션16", LocalTime.of(15, 30), 10),
                new MissionCandidate("미션17", LocalTime.of(16, 0), 10),
                new MissionCandidate("미션18", LocalTime.of(16, 30), 10),
                new MissionCandidate("미션19", LocalTime.of(17, 0), 10),
                new MissionCandidate("미션20", LocalTime.of(17, 30), 10)
        );
    }
}

package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopGroupCreatedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopMissionGenerateRequest;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionGenerateResponse;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionGenerateResponse.MissionCandidate;
import com.kkirok.server.domain.kkinipop.application.usecase.KkinipopUseCase;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMissionPolicy;
import com.kkirok.server.domain.kkinipop.exception.KkinipopErrorCode;
import com.kkirok.server.global.common.exception.InternalServerException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.global.external.openai.OpenAiService;
import com.kkirok.server.global.external.openai.prompt.PromptType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 끼니팝 미션 생성 전용 서비스.
 *
 * <p>동작 방식
 * - 매일 23:55에 다음날 사용할 공통 미션 후보 20개를 OpenAI로 생성한다.
 * - 생성된 후보 20개는 서비스 전체에서 공통으로 사용한다.
 * - 각 팀은 이 공통 후보 중 5개를 랜덤하게 배정받는다.
 * - 모든 후보 미션은 10분 동안 진행되는 동일한 형태의 미션이다.
 *
 * <p>랜덤 배정 예시
 * - 서비스 공통 후보가 20개 생성되면
 * - A팀은 [2, 4, 7, 11, 18]번 후보를 받을 수 있고
 * - B팀은 [1, 3, 7, 12, 20]번 후보를 받을 수 있다.
 * - 즉 제목/시간 후보 풀은 공통이지만, 팀별 선택 결과는 달라질 수 있다.
 *
 */
@Slf4j
@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class KkinipopMissionGenerateService {

    private final KkinipopMissionInsertService kkinipopMissionInsertService;
    private final DateTimeProvider dateTimeProvider;
    private final OpenAiService openAiService;
    private final KkinipopUseCase kkinipopUseCase;
    private final KkinipopMissionRepository missionRepository;
    private final KkinipopGroupRepository groupRepository;

    /**
     * 다음날 미션을 전체 그룹에 대해 생성한다.
     *
     * <p>예시
     * - 현재 시각이 2026-04-24 23:55 라면
     * - targetDate는 2026-04-25 이다.
     */
    public void generateNextDayMissions() {

        // 내일날짜
        LocalDate targetDate = dateTimeProvider.today().plusDays(1);

        // 그룹 조회
        List<KkinipopGroup> groups = kkinipopUseCase.findAllGroup();
        if (groups.isEmpty()) {
            return;
        }

        // 자정 근처에 즉시 배정(assignTodayMissionsForNewGroup)과 겹쳐 이미 targetDate 미션을 가진 그룹은 제외한다.
        Set<Long> groupIdsWithMissions = missionRepository
                .findMissionsBetween(targetDate.atStartOfDay(), targetDate.plusDays(1).atStartOfDay()).stream()
                .map(mission -> mission.getGroup().getId())
                .collect(Collectors.toSet());

        // 미션 생성하고
        List<MissionCandidate> sharedMissionPool = generateSharedMissionPool(targetDate);

        // 각 그룹에 미션 할당
        for (KkinipopGroup group : groups) {
            if (groupIdsWithMissions.contains(group.getId())) {
                continue;
            }
            assignRandomMissions(group, targetDate, sharedMissionPool);
        }
    }

    /**
     * OpenAI로부터 다음날 공통 미션 후보 풀을 만든다.
     *
     * <p>응답 예시
     * <pre>
     * {
     *   "missions": [
     *     {"title":"점심 식판 자랑하기","startTime":"12:00","durationMinutes":10}
     *   ]
     * }
     * </pre>
     *
     * <p>OpenAI 호출이 최대 {@value KkinipopMissionPolicy#MAX_GENERATION_ATTEMPTS}번 모두 실패하면
     * 최근 {@value KkinipopMissionPolicy#FALLBACK_LOOKBACK_DAYS}일간의 미션 이력으로 폴백 후보 풀을 생성한다.
     */
    public List<MissionCandidate> generateSharedMissionPool(LocalDate targetDate) {

        // openai 요청 준비
        KkinipopMissionGenerateRequest request = KkinipopMissionGenerateRequest.create(targetDate);

        for (int attempt = 1; attempt <= KkinipopMissionPolicy.MAX_GENERATION_ATTEMPTS; attempt++) {
            try {
                // openai 요청 후 응답 생성
                KkinipopMissionGenerateResponse response = openAiService.createObjectResponse(PromptType.KKINIPOP_MISSION, request, KkinipopMissionGenerateResponse.class);

                // 미션 풀 검증 ( OpenAI 응답의 개수와 시간 단위를 검증 )
                validateMissionPool(response);
                return response.missions();
            } catch (RuntimeException exception) {
                log.warn("끼니팝 미션 생성 OpenAI 호출 실패. attempt={}", attempt, exception);
            }
        }

        // OpenAI가 모두 실패하면 최근 미션 이력으로 폴백 후보 풀을 생성한다.
        return buildFallbackMissionPool(targetDate);
    }

    /**
     * OpenAI 호출이 모두 실패했을 때 최근 {@value KkinipopMissionPolicy#FALLBACK_LOOKBACK_DAYS}일간의
     * 미션 이력에서 서로 다른 (제목, 시작 시각) 조합을 후보로 삼아 폴백 풀을 만든다.
     *
     * <p>폴백 후보가 {@value KkinipopMissionPolicy#DAILY_MISSION_CANDIDATE_COUNT}개 미만이면
     * 미션 생성 실패로 처리한다.
     */
    private List<MissionCandidate> buildFallbackMissionPool(LocalDate targetDate) {
        LocalDateTime from = targetDate.minusDays(KkinipopMissionPolicy.FALLBACK_LOOKBACK_DAYS).atStartOfDay();
        LocalDateTime to = targetDate.atStartOfDay();

        List<MissionCandidate> candidates = toDistinctMissionCandidates(missionRepository.findMissionsBetween(from, to));
        List<MissionCandidate> fallbackPool = pickRandomCandidates(candidates, KkinipopMissionPolicy.DAILY_MISSION_CANDIDATE_COUNT);

        if (fallbackPool.size() != KkinipopMissionPolicy.DAILY_MISSION_CANDIDATE_COUNT) {
            throw new InternalServerException(KkinipopErrorCode.MISSION_GENERATION_FAILED);
        }

        return fallbackPool;
    }

    /**
     * 공통 후보 풀에서 팀별로 5개를 랜덤 배정한다.
     *
     * <p>랜덤 방식
     * - 공통 후보 20개를 섞은 뒤 앞에서 5개를 선택한다.
     * - 같은 팀 안에서는 중복 없이 5개가 저장된다.
     * - 모든 후보는 10분 미션이며, 30분 단위 시작 시각을 가진다.
     *   예: 09:00, 09:30, 18:00
     */
    private void assignRandomMissions(KkinipopGroup group, LocalDate targetDate, List<MissionCandidate> sharedMissionPool) {

        List<MissionCandidate> assignedCandidates = pickRandomCandidates(sharedMissionPool, KkinipopMissionPolicy.DAILY_MISSION_COUNT);

        if (assignedCandidates.size() != KkinipopMissionPolicy.DAILY_MISSION_COUNT) {
            throw new InternalServerException(KkinipopErrorCode.MISSION_GENERATION_FAILED);
        }

        createAndInsertMissions(group, targetDate, assignedCandidates);
    }

    /**
     * {@link KkinipopGroupCreatedEvent}를 그룹 생성 트랜잭션이 커밋된 뒤에 처리한다.
     *
     * <p>그룹 생성과 같은 트랜잭션에서 바로 미션을 배정하면, 아직 커밋되지 않은 group row를
     * 참조하는 FK 검증 때문에 같은 스레드 안에서 서로를 기다리는 self-deadlock이 발생할 수 있다.
     * 그래서 커밋 이후(AFTER_COMMIT)에 별도로 처리한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupCreated(KkinipopGroupCreatedEvent event) {
        assignTodayMissionsForNewGroup(groupRepository.getReferenceById(event.groupId()));
    }

    /**
     * 그룹이 새로 생성됐을 때, 오늘 이미 다른 그룹에 배정된 미션 이력에서 후보를 재사용해
     * OpenAI 호출 없이 즉시 오늘자 미션을 배정한다.
     *
     * <p>이미 시작한 미션은 후보에서 제외한다. 오늘자 후보가 5개 미만이면 일일 미션 수를 보장할 수 없으므로
     * 아무 미션도 배정하지 않는다.
     *
     * <p>미션 배정에 실패해도 그룹 생성 자체에는 영향이 없도록 예외를 삼킨다. {@link #handleGroupCreated}를 통해
     * 그룹 생성 트랜잭션이 커밋된 뒤 호출되는 것을 전제로 하며, 트랜잭션 안에서 직접 호출하면 안 된다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void assignTodayMissionsForNewGroup(KkinipopGroup group) {
        try {
            LocalDate today = dateTimeProvider.today();

            if (hasMissionsForDate(group.getId(), today)) {
                return;
            }

            LocalDateTime now = dateTimeProvider.now();

            List<KkinipopMission> notYetStartedMissions = missionRepository
                    .findMissionsBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay()).stream()
                    .filter(mission -> mission.getStartAt().isAfter(now))
                    .toList();
            List<MissionCandidate> todayMissionPool = toDistinctMissionCandidates(notYetStartedMissions);

            if (todayMissionPool.size() < KkinipopMissionPolicy.DAILY_MISSION_COUNT) {
                return;
            }

            List<MissionCandidate> assignedCandidates = pickRandomCandidates(todayMissionPool, KkinipopMissionPolicy.DAILY_MISSION_COUNT);
            createAndInsertMissions(group, today, assignedCandidates);
        } catch (RuntimeException exception) {
            log.warn("신규 그룹 즉시 미션 배정 실패. groupId={}", group.getId(), exception);
        }
    }

    private boolean hasMissionsForDate(Long groupId, LocalDate date) {
        return !missionRepository.findMissionsByDate(groupId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()).isEmpty();
    }

    private List<MissionCandidate> toDistinctMissionCandidates(List<KkinipopMission> missions) {
        return missions.stream()
                .map(mission -> new MissionCandidate(mission.getTitle(), mission.getStartAt().toLocalTime(), (int) mission.getDurationMinutes()))
                .distinct()
                .toList();
    }

    private List<MissionCandidate> pickRandomCandidates(List<MissionCandidate> pool, int count) {
        List<MissionCandidate> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        return shuffled.stream().limit(count).toList();
    }

    private void createAndInsertMissions(KkinipopGroup group, LocalDate date, List<MissionCandidate> candidates) {
        List<KkinipopMission> missions = candidates.stream()
                .map(candidate -> KkinipopMission.create(group, date, candidate))
                .toList();
        kkinipopMissionInsertService.insertMissions(missions);
    }

    /**
     * OpenAI 응답의 개수와 시간 단위를 검증한다.
     *
     * <p>검증 규칙
     * - 후보 수는 정확히 20개
     * - 모든 미션은 10분 동안 진행된다.
     * - 시작 시각의 분은 30분 단위
     *   예: 09:00, 09:30 가능 / 09:10, 09:45 불가
     * - 모든 시작 시각은 서로 달라야 한다.
     * - 제목은 공백·문장부호 포함 {@value KkinipopMissionPolicy#MISSION_TITLE_MAX_LENGTH}자를 넘지 않는다.
     */
    private void validateMissionPool(KkinipopMissionGenerateResponse response) {

        // OpenAI 응답이 비어 있거나 후보 개수가 20개가 아니면 실패한다.
        if (response == null || response.missions() == null
                || response.missions().size() != KkinipopMissionPolicy.DAILY_MISSION_CANDIDATE_COUNT) {
            throw new InternalServerException(KkinipopErrorCode.MISSION_GENERATION_FAILED);
        }

        // 각 미션은 서로 다른 시간대여야 하므로 시작 시각 중복을 허용하지 않는다.
        Set<LocalTime> uniqueStartTimes = response.missions().stream()
                .map(MissionCandidate::startTime)
                .collect(java.util.stream.Collectors.toSet());
        if (uniqueStartTimes.size() != response.missions().size()) {
            throw new InternalServerException(KkinipopErrorCode.MISSION_GENERATION_FAILED);
        }

        for (MissionCandidate mission : response.missions()) {
            // 시작 시각은 30분 단위 슬롯만 허용한다.
            if (mission.startTime().getMinute() % KkinipopMissionPolicy.REALTIME_SLOT_INTERVAL_MINUTES != 0) {
                throw new InternalServerException(KkinipopErrorCode.MISSION_GENERATION_FAILED);
            }

            // 모든 미션은 10분짜리 동일 규칙으로 생성되어야 한다.
            if (mission.durationMinutes() != KkinipopMissionPolicy.REALTIME_DURATION_MINUTES) {
                throw new InternalServerException(KkinipopErrorCode.MISSION_GENERATION_FAILED);
            }

            // 제목이 최대 길이를 넘으면 실패한다.
            if (mission.title().length() > KkinipopMissionPolicy.MISSION_TITLE_MAX_LENGTH) {
                throw new InternalServerException(KkinipopErrorCode.MISSION_GENERATION_FAILED);
            }
        }
    }

}

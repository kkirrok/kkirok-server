package com.kkirok.server.domain.notification.application.service;

import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.policy.MealReminderPolicy;
import com.kkirok.server.domain.notification.application.policy.MealReminderTarget;
import com.kkirok.server.domain.notification.domain.NotificationType;
import com.kkirok.server.global.common.util.DateTimeProvider;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MealReminderService {

    private final List<MealReminderPolicy> mealReminderPolicies;
    private final MemberRepository memberRepository;
    private final NotificationDispatchLogService notificationDispatchLogService;
    private final NotificationDispatcher notificationDispatcher;
    private final DateTimeProvider dateTimeProvider;

    public void runReminderCycle() {
        LocalDateTime now = dateTimeProvider.now();
        // 활동 시간대가 아니면 스케줄러가 돌더라도 바로 종료한다.
        if (!isWithinActiveHours(now)) {
            return;
        }

        // 앞 정책에서 이미 발송한 멤버는 뒤 정책에서 다시 잡지 않도록 누적한다.
        Set<Long> alreadyNotifiedMemberIds = new LinkedHashSet<>();
        for (MealReminderPolicy policy : mealReminderPolicies) {
            // 정책별 후보를 모은 뒤, 같은 사이클에서 중복된 멤버는 제외한다.
            List<MealReminderTarget> targets = policy.findTargets(now).stream()
                    .filter(target -> !alreadyNotifiedMemberIds.contains(target.memberId()))
                    .toList();

            if (targets.isEmpty()) {
                continue;
            }

            // 대상별 claim suffix를 미리 계산해, 이미 발송된 건을 한 번의 IN 조회로 걸러낸다.
            // 정책이 동일한 target을 중복 반환하더라도(현재는 발생하지 않지만) 예외 없이 하나만 남긴다.
            Map<MealReminderTarget, String> suffixByTarget = targets.stream()
                    .collect(java.util.stream.Collectors.toMap(t -> t, t -> claimSuffix(policy.type(), t), (a, b) -> a));
            Set<String> alreadyDispatchedSuffixes = notificationDispatchLogService.findAlreadyDispatchedSuffixes(
                    policy.type(), suffixByTarget.values());
            List<MealReminderTarget> pendingTargets = targets.stream()
                    .filter(target -> !alreadyDispatchedSuffixes.contains(suffixByTarget.get(target)))
                    .toList();

            if (pendingTargets.isEmpty()) {
                continue;
            }

            // 멤버 엔티티는 한 번에 읽어 N+1 조회를 피한다.
            Map<Long, Member> membersById = loadMembers(pendingTargets);
            for (MealReminderTarget target : pendingTargets) {
                Member member = membersById.get(target.memberId());
                if (member == null) {
                    continue;
                }

                // dispatch log를 먼저 선점해 같은 키의 재폴링을 막는다.
                if (!notificationDispatchLogService.claim(policy.type(), suffixByTarget.get(target))) {
                    continue;
                }

                alreadyNotifiedMemberIds.add(member.getId());
                notificationDispatcher.dispatchBatched(
                        List.of(member),
                        policy.type(),
                        policy.title(),
                        policy.body(),
                        buildData(policy.type(), target)
                );
            }
        }
    }

    private Map<Long, Member> loadMembers(List<MealReminderTarget> targets) {
        // 정책 결과의 memberId만 모아 bulk 조회용 키 집합을 만든다.
        Set<Long> memberIds = targets.stream()
                .map(MealReminderTarget::memberId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Member> membersById = new LinkedHashMap<>();
        for (Member member : memberRepository.findAllById(memberIds)) {
            membersById.put(member.getId(), member);
        }
        return membersById;
    }

    private boolean isWithinActiveHours(LocalDateTime now) {
        int hour = now.getHour();
        return hour >= 9 && hour < 22;
    }

    private String claimSuffix(NotificationType type, MealReminderTarget target) {
        // 오늘 미식사 정책은 멤버별로 하루 1회가 되도록 suffix에 memberId를 섞는다.
        if (type == NotificationType.MEAL_REMINDER_NO_TODAY) {
            return target.dispatchSuffix() + ":" + target.memberId();
        }
        // OVERDUE 정책은 같은 마지막 식사 기준으로 1시간에 한 번만 발송되도록 시간 슬롯을 붙인다.
        if (type == NotificationType.MEAL_REMINDER_OVERDUE) {
            String hourSlot = dateTimeProvider.now()
                    .truncatedTo(java.time.temporal.ChronoUnit.HOURS)
                    .toString();
            return target.dispatchSuffix() + ":" + hourSlot;
        }
        return target.dispatchSuffix();
    }

    private Map<String, String> buildData(NotificationType type, MealReminderTarget target) {
        // 클라이언트 라우팅용 payload를 정책별로 표준화해서 넣는다.
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", type.name());

        if (type == NotificationType.MEAL_REMINDER_OVERDUE) {
            data.put("lastMealId", target.dispatchSuffix());
            return data;
        }

        data.put("date", extractDate(target.dispatchSuffix()));
        return data;
    }

    private String extractDate(String dispatchSuffix) {
        // 정책에서 붙인 DAY- 접두사는 화면 전달용으로만 제거한다.
        if (dispatchSuffix.startsWith("DAY-")) {
            return dispatchSuffix.substring("DAY-".length());
        }
        return dispatchSuffix;
    }
}

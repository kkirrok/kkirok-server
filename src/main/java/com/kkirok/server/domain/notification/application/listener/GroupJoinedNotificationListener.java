package com.kkirok.server.domain.notification.application.listener;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopGroupJoinedEvent;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.service.NotificationDispatcher;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 그룹 가입 이벤트 발생 시 알림을 전송합니다. -> NotificationDispatcher.dispatchToMembers() 호출
 */
@Component
@RequiredArgsConstructor
public class GroupJoinedNotificationListener {

    private final KkinipopGroupRepository groupRepository;
    private final KkinipopGroupMemberRepository groupMemberRepository;
    private final MemberUseCase memberUseCase;
    private final NotificationDispatcher notificationDispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭션 성공한 뒤에만 알림 보내도록 함
    @Transactional(propagation = Propagation.REQUIRES_NEW) // dispatchToMembers()가 새 트랜잭션을 열지 않고 이 트랜잭션에 합류하도록 함 (MISSION_START와 동일 패턴). REQUIRED는 Spring이 TransactionalEventListener에서 허용하지 않음
    public void handle(KkinipopGroupJoinedEvent event) {

        List<Member> recipients = groupMemberRepository.findActiveGroupMembers(event.groupId()).stream()
                .map(KkinipopGroupMember::getMember)
                .filter(member -> !member.getId().equals(event.joinedMemberId()))
                .toList();

        if (recipients.isEmpty()) {
            return;
        }

        String groupName = groupRepository.findById(event.groupId())
                .map(KkinipopGroup::getName)
                .orElse("그룹");
        String joinedMemberName = memberUseCase.findMemberByMemberId(event.joinedMemberId()).getDisplayName();

        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", NotificationType.GROUP_JOIN.name());
        data.put("groupId", String.valueOf(event.groupId()));
        data.put("joinedMemberId", String.valueOf(event.joinedMemberId()));

        notificationDispatcher.dispatchToMembers(
                recipients,
                NotificationType.GROUP_JOIN,
                groupName,
                joinedMemberName + "님이 '" + groupName + "'에 참여하셨습니다.",
                data
        );
    }
}

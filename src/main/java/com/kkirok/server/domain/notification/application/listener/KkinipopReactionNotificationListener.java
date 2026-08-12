package com.kkirok.server.domain.notification.application.listener;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopReactionAddedEvent;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
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

@Component
@RequiredArgsConstructor
public class KkinipopReactionNotificationListener {

    private final MemberUseCase memberUseCase;
    private final KkinipopGroupRepository groupRepository;
    private final NotificationDispatcher notificationDispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW) // dispatchInstant()가 새 트랜잭션을 열지 않고 이 트랜잭션에 합류하도록 함 (MISSION_START와 동일 패턴). REQUIRED는 Spring이 TransactionalEventListener에서 허용하지 않음
    public void handle(KkinipopReactionAddedEvent event) {
        Member postAuthor = memberUseCase.findMemberByMemberId(event.postAuthorMemberId());
        Member reactor = memberUseCase.findMemberByMemberId(event.reactorMemberId());
        String groupName = groupRepository.findById(event.groupId())
                .map(KkinipopGroup::getName)
                .orElse("그룹");

        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", NotificationType.KKINIPOP_REACTION.name());
        data.put("postId", String.valueOf(event.postId()));
        data.put("groupId", String.valueOf(event.groupId()));
        data.put("reactorMemberId", String.valueOf(event.reactorMemberId()));
        data.put("emojiCode", event.emojiCode());
        data.put("isCustom", String.valueOf(event.customEmoji()));
        if (event.customEmoji()) {
            data.put("customEmojiImageKey", event.customEmojiImageKey());
        }

        notificationDispatcher.dispatchInstant(
                List.of(postAuthor),
                NotificationType.KKINIPOP_REACTION,
                groupName,
                reactor.getDisplayName() + "님이 이모지를 달았습니다",
                data
        );
    }
}

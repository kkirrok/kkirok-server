package com.kkirok.server.domain.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTest {

    private final Member member = MemberFixture.createLocalMember("테스터", "tester@test.com");

    @Test
    @DisplayName("생성 직후 retryCount는 0이다")
    void retryCountStartsAtZero() {
        Notification notification = Notification.create(member, NotificationType.GROUP_JOIN, "제목", "본문", null, null);

        assertThat(notification.getRetryCount()).isZero();
    }

    @Test
    @DisplayName("markFailed를 호출할 때마다 retryCount가 1씩 증가한다")
    void markFailedIncrementsRetryCount() {
        Notification notification = Notification.create(member, NotificationType.GROUP_JOIN, "제목", "본문", null, null);
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 12, 0);

        notification.markFailed(now, "NO_DEVICE");
        assertThat(notification.getRetryCount()).isEqualTo(1);

        notification.markFailed(now.plusMinutes(10), "FCM_NO_SUCCESS");
        assertThat(notification.getRetryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("markSent를 호출해도 retryCount는 그대로 유지된다")
    void markSentDoesNotResetRetryCount() {
        Notification notification = Notification.create(member, NotificationType.GROUP_JOIN, "제목", "본문", null, null);
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 12, 0);

        notification.markFailed(now, "NO_DEVICE");
        notification.markSent(now.plusMinutes(10));

        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getSentAt()).isEqualTo(now.plusMinutes(10));
        assertThat(notification.getFailedAt()).isNull();
    }
}

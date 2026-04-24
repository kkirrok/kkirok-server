package com.kkirok.server.global.webhook.sender;

import com.kkirok.server.global.webhook.DiscordWebhookPayload;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

// redis 오류로 인한 webhook 발생
// 에러가 발생한 원인을 body로 받아와서 출력
@Component
public class RedisWebhookSender extends WebhookSender {

    private static final int STACK_TRACE_MAX_LENGTH = 1000;
    private static final int RED_COLOR = 0xE03131;
    private static final String TITLE = "## 🚨 Redis Error";
    private static final String SCHEDULER_SOURCE_PREFIX = "scheduler/startup - ";

    @Override
    public void send(Object body) {
        sendDiscordPayload(buildPayload(body));
    }

    private DiscordWebhookPayload buildPayload(Object body) {
        return DiscordWebhookPayload.singleEmbed(TITLE, buildDescription(body), RED_COLOR);
    }

    private String buildDescription(Object body) {
        HttpServletRequest request = currentRequest();
        StringBuilder builder = new StringBuilder()
                .append(section("에러 발생 시간", currentTime()))
                .append(section("요청 엔드포인트", getEndpoint(request, SCHEDULER_SOURCE_PREFIX + body.getClass().getSimpleName())))
                .append(section("요청 클라이언트", getClient(request)));

        if (body instanceof Throwable throwable) {
            builder.append(section("에러 타입", throwable.getClass().getSimpleName()));
            if (throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
                builder.append(section("에러 메시지", throwable.getMessage()));
            }

            if (throwable instanceof DataAccessException dataAccessException) {
                Throwable cause = dataAccessException.getMostSpecificCause();
                if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
                    builder.append(section("원인", cause.getMessage()));
                }
            }

            String stackTrace = abbreviate(stackTraceOf(throwable), STACK_TRACE_MAX_LENGTH);
            if (!stackTrace.isBlank()) {
                builder.append(codeSection("에러 스택 트레이스", stackTrace));
            }

            return builder.toString();
        }

        builder.append(section("상세 정보", String.valueOf(body)));
        return builder.toString();
    }


}

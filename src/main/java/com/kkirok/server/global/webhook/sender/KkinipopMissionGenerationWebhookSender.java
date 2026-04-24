package com.kkirok.server.global.webhook.sender;

import com.kkirok.server.global.webhook.DiscordWebhookPayload;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

// 끼니팝 미션 생성 오류 발생 시 discord webhook 전송
@Component
public class KkinipopMissionGenerationWebhookSender extends WebhookSender {

    private static final int STACK_TRACE_MAX_LENGTH = 1000;
    private static final int RED_COLOR = 0xE03131;
    private static final String TITLE = "## 🚨 끼니팝 미션 생성 중 에러 발생";
    private static final String SCHEDULER_SOURCE = "scheduler - KkinipopMissionGenerateService";

    @Override
    public void send(Object body) {
        sendDiscordPayload(DiscordWebhookPayload.singleEmbed(TITLE, buildDescription(body), RED_COLOR));
    }

    private String buildDescription(Object body) {
        HttpServletRequest request = currentRequest();
        StringBuilder builder = new StringBuilder()
                .append(section("에러 발생 시간", currentTime()))
                .append(section("요청 엔드포인트", getEndpoint(request, SCHEDULER_SOURCE)))
                .append(section("요청 클라이언트", getClient(request)));

        if (body instanceof Throwable throwable) {
            builder.append(section("에러 타입", throwable.getClass().getSimpleName()));

            if (throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
                builder.append(section("에러 메시지", throwable.getMessage()));
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

package com.kkirok.server.global.webhook;

import java.util.List;
import java.util.Map;

// 웹훅 메시지를 생성합니다.
public record DiscordWebhookPayload(
        String content,
        List<DiscordEmbed> embeds
) {

    public static DiscordWebhookPayload singleEmbed(String content, String description, int color) {
        return new DiscordWebhookPayload(content, List.of(new DiscordEmbed(description, color)));
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "content", content,
                "embeds", embeds.stream().map(DiscordEmbed::toMap).toList()
        );
    }

    public record DiscordEmbed(
            String description,
            int color
    ) {
        public Map<String, Object> toMap() {
            return Map.of(
                    "description", description,
                    "color", color
            );
        }
    }
}

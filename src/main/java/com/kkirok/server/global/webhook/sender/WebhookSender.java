package com.kkirok.server.global.webhook.sender;

import com.kkirok.server.global.webhook.DiscordWebhookPayload;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// 웹훅 전송을 담당
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class WebhookSender {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분 ss초");
    private static final int DISCORD_CONTENT_MAX_LENGTH = 2000;
    private static final String TRUNCATED_SUFFIX = "\n... (truncated)";
    private static final String INTERNAL_CLIENT = "server-internal";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String REAL_IP_HEADER = "X-Real-IP";

    @Value("${webhook.discord.internal_server_url}")
    protected String url;

    protected void sendDiscordMessage(String content) {
        sendDiscordPayload(Map.of("content", truncate(content)));
    }

    protected void sendDiscordPayload(DiscordWebhookPayload payload) {
        sendDiscordPayload(payload.toMap());
    }

    protected void sendDiscordPayload(Map<String, Object> payload) {
        if (url == null || url.isBlank()) {
            log.warn("Discord webhook url is not configured. Skip sending webhook message.");
            return;
        }

        try {
            RestClient.create()
                    .post()
                    .uri(url)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            log.error("Failed to send discord webhook. payload={}", payload, exception);
        }
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }

        if (content.length() <= DISCORD_CONTENT_MAX_LENGTH) {
            return content;
        }

        int maxBodyLength = DISCORD_CONTENT_MAX_LENGTH - TRUNCATED_SUFFIX.length();
        return content.substring(0, Math.max(maxBodyLength, 0)) + TRUNCATED_SUFFIX;
    }

    protected HttpServletRequest currentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    protected String getEndpoint(HttpServletRequest request, String fallbackEndpoint) {
        if (request != null) {
            String queryString = request.getQueryString();
            if (queryString == null || queryString.isBlank()) {
                return request.getMethod() + " " + request.getRequestURI();
            }
            return request.getMethod() + " " + request.getRequestURI() + "?" + queryString;
        }
        return fallbackEndpoint;
    }

    protected String getClient(HttpServletRequest request) {
        if (request == null) {
            return INTERNAL_CLIENT;
        }

        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader(REAL_IP_HEADER);
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    protected String stackTraceOf(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }

    protected String abbreviate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    protected String currentTime() {
        return ZonedDateTime.now(KOREA_ZONE_ID).format(TIME_FORMATTER);
    }

    protected String section(String title, String value) {
        return "### " + title + "\n" + value + "\n";
    }

    protected String codeSection(String title, String value) {
        return "### " + title + "\n```text\n" + value + "\n```\n";
    }

    abstract public void send(Object body);

}

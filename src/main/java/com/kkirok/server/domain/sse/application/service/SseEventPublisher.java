package com.kkirok.server.domain.sse.application.service;

public interface SseEventPublisher {
    void publish(Long groupId, String eventName, Object payload);
}

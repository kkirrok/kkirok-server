package com.kkirok.server.domain.sse.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRegistryTest {

    private SseEmitterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SseEmitterRegistry();
    }

    @Test
    void subscribe_registersAndReturnsEmitter() throws Exception {
        SseEmitter emitter = registry.subscribe(1L);

        assertThat(emitters().get(1L)).containsExactly(emitter);
    }

    @Test
    void sendToMember_sendsToEveryRegisteredEmitter() throws Exception {
        SseEmitter first = registerMockEmitter(1L);
        SseEmitter second = registerMockEmitter(1L);

        registry.sendToMember(1L, "event", Map.of());

        verify(first).send(any(SseEmitter.SseEventBuilder.class));
        verify(second).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void sendToMember_doesNothingForUnsubscribedMember() {
        registry.sendToMember(1L, "event", Map.of());
    }

    @Test
    void completionCallback_removesEmitterAndLastEmitterRemovesMemberKey() throws Exception {
        SseEmitter emitter = registry.subscribe(1L);

        runCallback(emitter, "completionCallback");

        registry.sendToMember(1L, "event", Map.of());
        assertThat(emitters()).doesNotContainKey(1L);
    }

    @Test
    void timeoutCallback_removesEmitter() throws Exception {
        SseEmitter emitter = registry.subscribe(1L);

        runCallback(emitter, "timeoutCallback");

        assertThat(emitters()).doesNotContainKey(1L);
    }

    @Test
    void errorCallback_removesEmitter() throws Exception {
        SseEmitter emitter = registry.subscribe(1L);
        Field callback = emitter.getClass().getSuperclass().getDeclaredField("errorCallback");
        callback.setAccessible(true);
        ((java.util.function.Consumer<Throwable>) callback.get(emitter)).accept(new IOException());

        assertThat(emitters()).doesNotContainKey(1L);
    }

    @Test
    void sendToMember_removesEmitterWhenSendFails() throws Exception {
        SseEmitter emitter = registerMockEmitter(1L);
        doThrow(new IOException()).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        registry.sendToMember(1L, "event", Map.of());

        assertThat(emitters()).doesNotContainKey(1L);
    }

    @Test
    void broadcastToAll_sendsToAllRegisteredEmitters() throws Exception {
        SseEmitter first = registerMockEmitter(1L);
        SseEmitter second = registerMockEmitter(2L);

        registry.broadcastToAll("ping", Map.of());

        verify(first).send(any(SseEmitter.SseEventBuilder.class));
        verify(second).send(any(SseEmitter.SseEventBuilder.class));
    }

    private SseEmitter registerMockEmitter(Long memberId) throws Exception {
        SseEmitter emitter = mock(SseEmitter.class);
        emitters().computeIfAbsent(memberId, ignored -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(emitter);
        return emitter;
    }

    private void runCallback(SseEmitter emitter, String callbackName) throws Exception {
        Field callback = emitter.getClass().getSuperclass().getDeclaredField(callbackName);
        callback.setAccessible(true);
        ((Runnable) callback.get(emitter)).run();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, List<SseEmitter>> emitters() throws Exception {
        Field field = SseEmitterRegistry.class.getDeclaredField("emitters");
        field.setAccessible(true);
        return (ConcurrentHashMap<Long, List<SseEmitter>>) field.get(registry);
    }
}

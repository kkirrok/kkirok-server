package com.kkirok.server.domain.sse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.kkirok.server.domain.sse.application.service.SseEmitterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseControllerTest {
    @Mock private SseEmitterRegistry registry;
    @Mock private SseEmitter emitter;
    @InjectMocks private SseController controller;

    @Test
    void subscribe_registersMemberAndSendsConnectedEvent() throws Exception {
        given(registry.subscribe(1L)).willReturn(emitter);

        SseEmitter response = controller.subscribe(1L);

        assertThat(response).isSameAs(emitter);
        then(registry).should().subscribe(1L);
        then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
    }
}

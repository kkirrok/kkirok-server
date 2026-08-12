package com.kkirok.server.domain.sse.api;

import com.kkirok.server.domain.sse.application.service.SseEmitterRegistry;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/sse")
@RoleUserAuth
public class SseController {

    private final SseEmitterRegistry registry;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@CurrentMember Long memberId) {
        SseEmitter emitter = registry.subscribe(memberId);
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of()));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE connected event: memberId={}", memberId, e);
            emitter.completeWithError(e);
        }
        return emitter;
    }
}

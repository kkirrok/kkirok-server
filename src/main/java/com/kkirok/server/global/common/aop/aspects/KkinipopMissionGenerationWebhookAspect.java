package com.kkirok.server.global.common.aop.aspects;

import com.kkirok.server.global.webhook.sender.KkinipopMissionGenerationWebhookSender;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(4)
@Profile("!test")
public class KkinipopMissionGenerationWebhookAspect extends ExceptionWebhookAspect {

    // 끼니팝 웹훅 sender를 주입
    public KkinipopMissionGenerationWebhookAspect(KkinipopMissionGenerationWebhookSender webhookSender) {
        super(webhookSender);
    }

    @AfterThrowing(
            pointcut = "com.kkirok.server.global.common.aop.Pointcuts.kkinipopMissionGeneration()",
            throwing = "ex"
    )
    public void sendKkinipopMissionGenerationWebhook(Exception ex) {
        sendWebhook(ex);
    }
}

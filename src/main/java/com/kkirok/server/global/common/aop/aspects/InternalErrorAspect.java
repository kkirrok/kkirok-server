package com.kkirok.server.global.common.aop.aspects;

import com.kkirok.server.global.common.exception.KkirokException;
import com.kkirok.server.global.webhook.InternalServerWebhook;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(4)
@Profile("!test")
public class InternalErrorAspect extends ExceptionWebhookAspect {

    public InternalErrorAspect(InternalServerWebhook internalServerWebhook) {
        super(internalServerWebhook);
    }

    @AfterThrowing(
            pointcut = "com.kkirok.server.global.common.aop.Pointcuts.allController()",
            throwing = "ex"
    )
    public void sendInternalErrorWebhook(Exception ex) {
        if (ex instanceof KkirokException) {
            return;
        }
        sendWebhook(ex);
    }
}

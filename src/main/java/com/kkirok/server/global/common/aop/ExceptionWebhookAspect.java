package com.kkirok.server.global.common.aop;

import com.kkirok.server.global.webhook.WebhookSender;

// 웹훅을 위한 aspect
public abstract class ExceptionWebhookAspect {

    private final WebhookSender webhookSender;

    protected ExceptionWebhookAspect(WebhookSender webhookSender) {
        this.webhookSender = webhookSender;
    }

    protected void sendWebhook(Object body) {
        webhookSender.send(body);
    }
}

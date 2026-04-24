package com.kkirok.server.global.common.aop.aspects;

import com.kkirok.server.global.common.redis.exception.RedisException;
import com.kkirok.server.global.webhook.sender.RedisWebhookSender;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(4)
@Profile("!test")
public class RedisWebhookAspect extends ExceptionWebhookAspect {

    public RedisWebhookAspect(RedisWebhookSender redisWebhookSender) {
        super(redisWebhookSender);
    }

    @AfterThrowing(
            pointcut = "com.kkirok.server.global.common.aop.Pointcuts.allRedisAccess()",
            throwing = "ex"
    )
    public void sendRedisWebhook(RedisException ex) {
        sendWebhook(ex);
    }
}

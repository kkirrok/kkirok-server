package com.kkirok.server.global.webhook;

import com.kkirok.server.global.webhook.sender.WebhookSender;
import org.springframework.stereotype.Component;

// 500 에러로 인한 웹훅 발생
// 호출 유저의 email, endpoint, 호출 시간, 클라이언트 정보(ip, email), 에러 스택트레이스를 출력
@Component
public class InternalServerWebhook extends WebhookSender {

    @Override
    public void send(Object ex) {

    }

}

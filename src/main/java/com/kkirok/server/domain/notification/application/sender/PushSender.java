package com.kkirok.server.domain.notification.application.sender;

import java.util.List;

public interface PushSender {

    PushResult sendMulticast(List<String> tokens, PushPayload payload);
}

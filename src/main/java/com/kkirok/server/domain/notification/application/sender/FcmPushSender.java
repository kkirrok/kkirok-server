package com.kkirok.server.domain.notification.application.sender;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 실제로 알림을 발송하는 역할
 */
@Service
@RequiredArgsConstructor
public class FcmPushSender implements PushSender {

    private static final int FCM_MAX_TOKENS = 500;

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public PushResult sendMulticast(List<String> tokens, PushPayload payload) {
        if (CollectionUtils.isEmpty(tokens)) {
            return new PushResult(0, Collections.emptyList());
        }

        int successCount = 0;
        List<String> invalidTokens = new ArrayList<>();

        for (List<String> chunk : partition(tokens, FCM_MAX_TOKENS)) {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(chunk)
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(payload.title())
                                    .setBody(payload.body())
                                    .build()
                    )
                    .putAllData(payload.data())
                    .build();

            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
                successCount += response.getSuccessCount();
                collectInvalidTokens(chunk, response, invalidTokens);
            } catch (FirebaseMessagingException e) {
                throw new IllegalStateException("FCM send failed", e);
            }
        }

        return new PushResult(successCount, List.copyOf(invalidTokens));
    }

    private void collectInvalidTokens(List<String> tokens, BatchResponse response, List<String> invalidTokens) {
        List<SendResponse> responses = response.getResponses();
        for (int index = 0; index < responses.size(); index++) {
            SendResponse sendResponse = responses.get(index);
            if (sendResponse.isSuccessful() || sendResponse.getException() == null) {
                continue;
            }

            MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                invalidTokens.add(tokens.get(index));
            }
        }
    }

    private List<List<String>> partition(List<String> tokens, int chunkSize) {
        if (tokens.size() <= chunkSize) {
            return List.of(tokens);
        }

        List<List<String>> chunks = new ArrayList<>();
        for (int start = 0; start < tokens.size(); start += chunkSize) {
            int end = Math.min(start + chunkSize, tokens.size());
            chunks.add(tokens.subList(start, end));
        }
        return chunks;
    }
}

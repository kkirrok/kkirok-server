package com.kkirok.server.domain.notification.application.sender;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
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
                                    .setImage(payload.imageUrl())
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

    private <T> List<List<T>> partition(List<T> items, int chunkSize) {
        if (items.size() <= chunkSize) {
            return List.of(items);
        }

        List<List<T>> chunks = new ArrayList<>();
        for (int start = 0; start < items.size(); start += chunkSize) {
            int end = Math.min(start + chunkSize, items.size());
            chunks.add(items.subList(start, end));
        }
        return chunks;
    }

    @Override
    public List<PushBatchItemResult> sendEach(List<PushBatchMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return List.of();
        }

        List<PushBatchItemResult> results = new ArrayList<>();

        for (List<PushBatchMessage> chunk : partition(messages, FCM_MAX_TOKENS)) {
            List<Message> fcmMessages = chunk.stream()
                    .map(this::toMessage)
                    .toList();

            try {
                BatchResponse response = firebaseMessaging.sendEach(fcmMessages);
                List<SendResponse> sendResponses = response.getResponses();
                for (int index = 0; index < chunk.size(); index++) {
                    results.add(toItemResult(chunk.get(index).token(), sendResponses.get(index)));
                }
            } catch (FirebaseMessagingException e) {
                throw new IllegalStateException("FCM batch send failed", e);
            }
        }

        return results;
    }

    private Message toMessage(PushBatchMessage batchMessage) {
        PushPayload payload = batchMessage.payload();
        return Message.builder()
                .setToken(batchMessage.token())
                .setNotification(
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle(payload.title())
                                .setBody(payload.body())
                                .setImage(payload.imageUrl())
                                .build()
                )
                .putAllData(payload.data())
                .build();
    }

    private PushBatchItemResult toItemResult(String token, SendResponse sendResponse) {
        if (sendResponse.isSuccessful()) {
            return new PushBatchItemResult(token, true, false);
        }

        MessagingErrorCode errorCode = sendResponse.getException() == null
                ? null
                : sendResponse.getException().getMessagingErrorCode();
        boolean invalidToken = errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
        return new PushBatchItemResult(token, false, invalidToken);
    }
}

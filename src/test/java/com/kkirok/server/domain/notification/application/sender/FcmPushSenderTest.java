package com.kkirok.server.domain.notification.application.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmPushSenderTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private FcmPushSender fcmPushSender;

    private final PushPayload payload = new PushPayload("제목", "본문", null, Map.of("type", "GROUP_JOIN"));

    @Test
    @DisplayName("빈 목록이면 FCM을 호출하지 않고 빈 결과를 반환한다")
    void sendEach_empty() {
        List<PushBatchItemResult> results = fcmPushSender.sendEach(List.of());

        assertThat(results).isEmpty();
        then(firebaseMessaging).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("전부 성공하면 각 토큰별로 success=true 결과를 순서대로 반환한다")
    void sendEach_allSuccess() throws FirebaseMessagingException {
        List<PushBatchMessage> messages = List.of(
                new PushBatchMessage("token-1", payload),
                new PushBatchMessage("token-2", payload)
        );

        SendResponse success1 = mockSuccess();
        SendResponse success2 = mockSuccess();
        BatchResponse batchResponse = mockBatchResponse(List.of(success1, success2));
        given(firebaseMessaging.sendEach(anyList())).willReturn(batchResponse);

        List<PushBatchItemResult> results = fcmPushSender.sendEach(messages);

        assertThat(results).containsExactly(
                new PushBatchItemResult("token-1", true, false),
                new PushBatchItemResult("token-2", true, false)
        );
    }

    @Test
    @DisplayName("UNREGISTERED 에러는 invalidToken=true로 표시한다")
    void sendEach_unregisteredToken() throws FirebaseMessagingException {
        List<PushBatchMessage> messages = List.of(new PushBatchMessage("dead-token", payload));

        SendResponse failure = mockFailure(MessagingErrorCode.UNREGISTERED);
        BatchResponse batchResponse = mockBatchResponse(List.of(failure));
        given(firebaseMessaging.sendEach(anyList())).willReturn(batchResponse);

        List<PushBatchItemResult> results = fcmPushSender.sendEach(messages);

        assertThat(results).containsExactly(new PushBatchItemResult("dead-token", false, true));
    }

    @Test
    @DisplayName("UNREGISTERED/INVALID_ARGUMENT가 아닌 에러는 invalidToken=false로 표시한다")
    void sendEach_otherFailure() throws FirebaseMessagingException {
        List<PushBatchMessage> messages = List.of(new PushBatchMessage("token-1", payload));

        SendResponse failure = mockFailure(MessagingErrorCode.INTERNAL);
        BatchResponse batchResponse = mockBatchResponse(List.of(failure));
        given(firebaseMessaging.sendEach(anyList())).willReturn(batchResponse);

        List<PushBatchItemResult> results = fcmPushSender.sendEach(messages);

        assertThat(results).containsExactly(new PushBatchItemResult("token-1", false, false));
    }

    @Test
    @DisplayName("500개를 초과하면 500개씩 청크로 나눠 여러 번 호출하고 결과를 원래 순서대로 합친다")
    void sendEach_chunksOver500() throws FirebaseMessagingException {
        List<PushBatchMessage> messages = new java.util.ArrayList<>();
        for (int i = 0; i < 600; i++) {
            messages.add(new PushBatchMessage("token-" + i, payload));
        }

        List<SendResponse> firstChunkResponses = new java.util.ArrayList<>();
        for (int i = 0; i < 500; i++) {
            firstChunkResponses.add(mockSuccess());
        }
        List<SendResponse> secondChunkResponses = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            secondChunkResponses.add(mockSuccess());
        }

        BatchResponse firstBatch = mockBatchResponse(firstChunkResponses);
        BatchResponse secondBatch = mockBatchResponse(secondChunkResponses);
        given(firebaseMessaging.sendEach(anyList())).willReturn(firstBatch, secondBatch);

        List<PushBatchItemResult> results = fcmPushSender.sendEach(messages);

        assertThat(results).hasSize(600);
        assertThat(results.get(0).token()).isEqualTo("token-0");
        assertThat(results.get(599).token()).isEqualTo("token-599");
        assertThat(results).allSatisfy(result -> assertThat(result.success()).isTrue());

        ArgumentCaptor<List<Message>> captor = ArgumentCaptor.forClass(List.class);
        then(firebaseMessaging).should(org.mockito.Mockito.times(2)).sendEach(captor.capture());
        assertThat(captor.getAllValues().get(0)).hasSize(500);
        assertThat(captor.getAllValues().get(1)).hasSize(100);
    }

    private SendResponse mockSuccess() {
        SendResponse sendResponse = org.mockito.Mockito.mock(SendResponse.class);
        given(sendResponse.isSuccessful()).willReturn(true);
        return sendResponse;
    }

    private SendResponse mockFailure(MessagingErrorCode errorCode) {
        FirebaseMessagingException exception = org.mockito.Mockito.mock(FirebaseMessagingException.class);
        given(exception.getMessagingErrorCode()).willReturn(errorCode);

        SendResponse sendResponse = org.mockito.Mockito.mock(SendResponse.class);
        given(sendResponse.isSuccessful()).willReturn(false);
        given(sendResponse.getException()).willReturn(exception);
        return sendResponse;
    }

    private BatchResponse mockBatchResponse(List<SendResponse> responses) {
        BatchResponse batchResponse = org.mockito.Mockito.mock(BatchResponse.class);
        given(batchResponse.getResponses()).willReturn(responses);
        return batchResponse;
    }
}

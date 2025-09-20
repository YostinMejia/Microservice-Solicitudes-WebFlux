package co.com.bancolombia.sqs.sender.senders;

import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateNotificationGateway;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import co.com.bancolombia.sqs.sender.exceptions.ValidationErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class NotificationSQSSender implements StateNotificationGateway {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.notificationQueueUrl())
                .messageBody(message)
                .build();
    }

    @Override
    public Mono<String> notifyStateUpdate(UUID idApplication, String newState, String userEmail) {
        return createMessageFromState(idApplication, newState, userEmail)
                .map(this::buildRequest)
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }


    private Mono<String> createMessageFromState(UUID idApplication, String newState, String userEmail) {
        return Mono.fromCallable(() -> {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> payload = new HashMap<>();
            payload.put("userEmail", userEmail);
            payload.put("newState", newState);
            payload.put("idApplication", idApplication);

            return mapper.writeValueAsString(payload);
        }).onErrorMap(e -> new BusinessException(ValidationErrorMessages.JSON_PARSE_FAILED));
    }

}

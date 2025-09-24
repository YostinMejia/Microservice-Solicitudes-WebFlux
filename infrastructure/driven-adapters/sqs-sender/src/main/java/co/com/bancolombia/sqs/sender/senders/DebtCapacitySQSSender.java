package co.com.bancolombia.sqs.sender.senders;

import co.com.bancolombia.model.application.gateways.DebtCapacityGateway;
import co.com.bancolombia.model.exceptions.BusinessException;
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
public class DebtCapacitySQSSender implements DebtCapacityGateway {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.debtCapacityQueueUrl())
                .messageBody(message)
                .build();
    }


    @Override
    public Mono<String> loanDecision(Long totalIncome, double currentMonthlyDebt, double interestRate, int termMonths, float loanAmount, UUID idApplication,String email) {

        return createMessageFromState(totalIncome, currentMonthlyDebt, interestRate, termMonths, loanAmount, idApplication ,email)
                .map(this::buildRequest)
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);

    }

    private Mono<String> createMessageFromState(double totalIncome, double currentMonthlyDebt, double interestRate, int termMonths, float loanAmount, UUID idApplication,String email) {
        return Mono.fromCallable(() -> {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> payload = new HashMap<>();
            payload.put("totalIncome", totalIncome);
            payload.put("currentMonthlyDebt", currentMonthlyDebt);
            payload.put("interestRate", interestRate);
            payload.put("termMonths", termMonths);
            payload.put("loanAmount", loanAmount);
            payload.put("idApplication", idApplication);
            payload.put("email", email);

            return mapper.writeValueAsString(payload);
        }).onErrorMap(e -> new BusinessException(ValidationErrorMessages.JSON_PARSE_FAILED));
    }


}

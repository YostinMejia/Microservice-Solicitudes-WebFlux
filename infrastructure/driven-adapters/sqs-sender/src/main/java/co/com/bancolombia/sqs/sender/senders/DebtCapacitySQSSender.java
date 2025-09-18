package co.com.bancolombia.sqs.sender.senders;

import co.com.bancolombia.model.application.gateways.DebtCapacityGateway;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateNotificationGateway;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

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
    public Mono<String> loanDecision(double totalIncome, double currentMonthlyDebt, double interestRate, int termMonths, float loanAmount) {
        String message = createMessageFromState(totalIncome,currentMonthlyDebt,interestRate,termMonths,loanAmount);
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);

    }

    private String createMessageFromState(double totalIncome, double currentMonthlyDebt, double interestRate, int termMonths, float loanAmount) {
        return String.format(
                "{" +
                        "\"totalIncome\": %.2f, " +
                        "\"currentMonthlyDebt\": %.2f, " +
                        "\"interestRate\": %.4f, " +
                        "\"termMonths\": %d, " +
                        "\"loanAmount\": %.2f" +
                        "}",
                totalIncome,
                currentMonthlyDebt,
                interestRate,
                termMonths,
                loanAmount
        );
    }

}

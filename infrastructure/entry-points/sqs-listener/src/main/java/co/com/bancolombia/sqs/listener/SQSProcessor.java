package co.com.bancolombia.sqs.listener;

import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.sqs.listener.dto.UpdateState;
import co.com.bancolombia.sqs.listener.exceptions.ValidationErrorMessages;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final ApplicationUseCase applicationUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> apply(Message message) {
        System.out.println(message.body());
        log.info("{} received message from sqs", message.body());
        return parseBody(message.body()).zipWhen(dto ->applicationUseCase.updateApplicationState(dto.idApplication(),dto.state()))
                .doOnNext(dto-> log.info("dto received {}", dto.getT1()))
                .flatMap(tuple -> applicationUseCase.notifyUpdate(tuple.getT1().idApplication(),tuple.getT1().state(),tuple.getT2().getEmail()))
                .thenEmpty(Mono.empty());

    }

    private Mono<UpdateState> parseBody(String body) {
        return Mono.fromCallable(() -> objectMapper.readValue(body, UpdateState.class))
                .onErrorMap(e -> new BusinessException(ValidationErrorMessages.UPDATE_JSON_PARSE_FAILED));
    }

}

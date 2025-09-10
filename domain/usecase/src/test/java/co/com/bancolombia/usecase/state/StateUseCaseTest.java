package co.com.bancolombia.usecase.state;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.States;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StateUseCaseTest {

    @InjectMocks
    private StateUseCase stateUseCase;

    @Mock
    private StateRepository stateRepository;

    private final UUID stateId = UUID.randomUUID();

    @Test
    void givenUpdate_whenStateIsNotFound_thenShouldReturnError(){

        given(stateRepository.findById(stateId)).willReturn(Mono.empty());

        Mono<State> result = stateUseCase.update(stateId, States.APPROVED.getValue());

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                ((BusinessException) throwable).getCode().equals(BusinessErrorCode.STATE_NOT_FOUND.getBusinessCode()) &&
                                throwable.getMessage().equals(BusinessErrorCode.STATE_NOT_FOUND.getMessage())
                )
                .verify();
    }

    @Test
    void givenUpdate_whenStateIsFound_thenShouldUpdateIt(){

        State state = State.builder()
                .name(States.APPROVED.getValue())
                .description("")
                .id(stateId)
                .build();

        given(stateRepository.findById(stateId)).willReturn(Mono.just(state));
        State stateUpdated = state.toBuilder().name(States.REJECTED.getValue()).build();

        given(stateRepository.update(any(State.class))).willAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<State> result = stateUseCase.update(stateId, States.REJECTED.getValue());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getName().equals(stateUpdated.getName()) && response.getId().equals(state.getId()))
                .verifyComplete();
    }
}
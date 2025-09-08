package co.com.bancolombia.usecase.state;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class StateUseCase {

    private final StateRepository stateRepository;

    public Mono<State> update(UUID id, String state) {
        return stateRepository.findById(id)
                .flatMap(stateFound -> {
                    final State newState = stateFound.toBuilder().name(state).build();
                    return stateRepository.update(newState);
                })
                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.STATE_NOT_FOUND)));
    }
}

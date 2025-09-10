package co.com.bancolombia.model.state.gateways;

import co.com.bancolombia.model.state.State;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StateRepository {
    Mono<State> save(State state);
    Mono<State> update(State state);
    Mono<State> findById(UUID id);
}
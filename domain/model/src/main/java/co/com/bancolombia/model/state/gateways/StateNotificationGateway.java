package co.com.bancolombia.model.state.gateways;

import co.com.bancolombia.model.state.State;
import reactor.core.publisher.Mono;

public interface StateNotificationGateway {
    Mono<String> notifyStateUpdate(State state, String userEmail);
}

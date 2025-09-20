package co.com.bancolombia.model.state.gateways;

import co.com.bancolombia.model.state.State;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StateNotificationGateway {
    Mono<String> notifyStateUpdate(UUID idApplication, String newState, String userEmail);
}

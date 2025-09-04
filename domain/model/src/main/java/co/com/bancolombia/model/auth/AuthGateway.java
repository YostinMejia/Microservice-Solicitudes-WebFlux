package co.com.bancolombia.model.auth;

import reactor.core.publisher.Mono;

public interface AuthGateway {
    Mono<Boolean> isSameEmailAsToken(String email, String authHeader);
}

package co.com.bancolombia.model.auth.gateway;

import reactor.core.publisher.Mono;

public interface AuthGateway {
    Mono<Boolean> isSameEmailAsToken(String email, String authHeader);
    Mono<String> getRolByAuthHeaderToken(String authHeader);
}

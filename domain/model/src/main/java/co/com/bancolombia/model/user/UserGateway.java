package co.com.bancolombia.model.user;

import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<Boolean> existByDocumentAndEmail(String document, String email, String authHeader);
}

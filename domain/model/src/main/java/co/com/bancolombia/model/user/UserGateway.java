package co.com.bancolombia.model.user;

import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<Boolean> existByDocument(String document);
}

package co.com.bancolombia.model.user;

import reactor.core.publisher.Mono;

public interface UserQueryGateway {
    Mono<Boolean> existByDocument(String document);
}

package co.com.bancolombia.model;

import reactor.core.publisher.Mono;

public interface TransactionalOperatorGateway {
    <T> Mono<T> execute(Mono<T> mono);
}
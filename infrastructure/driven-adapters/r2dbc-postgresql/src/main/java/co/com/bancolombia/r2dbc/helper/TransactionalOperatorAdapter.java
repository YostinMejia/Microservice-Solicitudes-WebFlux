package co.com.bancolombia.r2dbc.helper;

import co.com.bancolombia.model.TransactionalOperatorGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TransactionalOperatorAdapter implements TransactionalOperatorGateway {
    private final TransactionalOperator transactionalOperator;

    @Override
    public <T> Mono<T> execute(Mono<T> mono) {

        return transactionalOperator.execute(transactionStatus -> mono).next();
    }
}
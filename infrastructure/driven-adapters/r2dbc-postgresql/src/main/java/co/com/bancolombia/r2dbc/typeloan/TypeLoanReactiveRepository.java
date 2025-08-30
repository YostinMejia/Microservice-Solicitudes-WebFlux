package co.com.bancolombia.r2dbc.typeloan;

import co.com.bancolombia.model.typeloan.TypeLoan;
import co.com.bancolombia.r2dbc.typeloan.entity.TypeLoanEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TypeLoanReactiveRepository extends ReactiveCrudRepository<TypeLoanEntity, String>, ReactiveQueryByExampleExecutor<TypeLoanEntity> {
    Mono<TypeLoan> findByName(String name);
}

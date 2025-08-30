package co.com.bancolombia.model.typeloan.gateways;

import co.com.bancolombia.model.typeloan.TypeLoan;
import reactor.core.publisher.Mono;

public interface TypeLoanRepository {
    Mono<TypeLoan> findByName(String name);
}

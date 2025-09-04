package co.com.bancolombia.r2dbc.typeloan;

import co.com.bancolombia.model.typeloan.TypeLoan;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import co.com.bancolombia.r2dbc.typeloan.entity.TypeLoanEntity;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
public class TypeLoanReactiveRepositoryAdapter extends ReactiveAdapterOperations<TypeLoan, TypeLoanEntity, String, TypeLoanReactiveRepository> implements TypeLoanRepository {
    public TypeLoanReactiveRepositoryAdapter(TypeLoanReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, d -> mapper.map(d, TypeLoan.class));
    }
    @Override
    public Mono<TypeLoan> findByName(String name) {
        return repository.findByName(name);
    }

}

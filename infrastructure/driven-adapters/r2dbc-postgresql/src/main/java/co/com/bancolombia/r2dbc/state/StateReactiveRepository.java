package co.com.bancolombia.r2dbc.state;

import co.com.bancolombia.r2dbc.state.entity.StateEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface StateReactiveRepository extends ReactiveCrudRepository<StateEntity, String>, ReactiveQueryByExampleExecutor<StateEntity> {

}
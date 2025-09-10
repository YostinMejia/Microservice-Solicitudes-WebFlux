package co.com.bancolombia.r2dbc.state;

import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import co.com.bancolombia.r2dbc.state.entity.StateEntity;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class StateReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        State,
        StateEntity,
        String,
        StateReactiveRepository
        > implements StateRepository {
    public StateReactiveRepositoryAdapter(StateReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, State.class));
    }

}

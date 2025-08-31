package co.com.bancolombia.r2dbc.state;

import co.com.bancolombia.model.state.State;
import co.com.bancolombia.r2dbc.state.entity.StateEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StateReactiveRepositoryAdapterTest {

    @InjectMocks
    StateReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    StateReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Test
    void mustSaveState() {
        // Arrange
        State state = new State();
        StateEntity stateEntity = new StateEntity();

        when(repository.save(any(StateEntity.class))).thenReturn(Mono.just(stateEntity));
        when(mapper.map(any(State.class), eq(StateEntity.class))).thenReturn(stateEntity);
        when(mapper.map(stateEntity, State.class)).thenReturn(state);

        // Act
        Mono<State> result = repositoryAdapter.save(state);

        // Assert
        StepVerifier.create(result)
                .expectNext(state)
                .verifyComplete();
    }
}
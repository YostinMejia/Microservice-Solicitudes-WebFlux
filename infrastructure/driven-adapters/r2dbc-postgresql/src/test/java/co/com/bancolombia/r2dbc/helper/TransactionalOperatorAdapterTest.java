package co.com.bancolombia.r2dbc.helper;
import co.com.bancolombia.r2dbc.helper.TransactionalOperatorAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionalOperatorAdapterTest {

    @InjectMocks
    TransactionalOperatorAdapter transactionalOperatorAdapter;

    @Mock
    TransactionalOperator transactionalOperator;

    @Test
    void mustExecuteMonoInTransaction() {
        // Arrange
        // Simula la ejecución del 'TransactionalOperator' para que devuelva el resultado esperado.
        // El método 'execute' de la clase real devuelve un Flux, por lo que el mock debe hacer lo mismo.
        when(transactionalOperator.execute(any()))
                .thenReturn(Flux.just("test"));

        // Act
        Mono<String> result = transactionalOperatorAdapter.execute(Mono.just("test"));

        // Assert
        StepVerifier.create(result)
                .expectNext("test")
                .verifyComplete();
    }
}
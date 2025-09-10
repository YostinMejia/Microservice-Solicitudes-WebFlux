package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationDetails;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.dto.PaginationParams;
import co.com.bancolombia.model.state.States;
import co.com.bancolombia.r2dbc.application.ApplicationReactiveRepository;
import co.com.bancolombia.r2dbc.application.ApplicationReactiveRepositoryAdapter;
import co.com.bancolombia.r2dbc.application.entity.ApplicationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationReactiveRepositoryAdapterTest {

    @InjectMocks
    ApplicationReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    ApplicationReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Mock
    TransactionalOperator transactionalOperator;

    @Mock
    DatabaseClient databaseClient;

    @Test
    void  findById_shouldReturnApplication_whenEntityExists() {
        // Arrange
        ApplicationEntity entity = new ApplicationEntity();
        Application domain = new Application();

        when(repository.findById("123")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Application.class)).thenReturn(domain);

        // Act & Assert
        StepVerifier.create(repositoryAdapter.findById("123"))
                .expectNext(domain)
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllApplications_whenEntitiesExist() {
        // Arrange
        ApplicationEntity entity = new ApplicationEntity();
        Application domain = new Application();

        when(repository.findAll()).thenReturn(Flux.just(entity));
        when(mapper.map(entity, Application.class)).thenReturn(domain);

        // Act & Assert
        StepVerifier.create(repositoryAdapter.findAll())
                .expectNext(domain)
                .verifyComplete();
    }

    @Test
    void findByExample_shouldReturnApplications_whenMatchingEntitiesExist() {
        // Arrange
        ApplicationEntity entity = new ApplicationEntity();
        Application domain = new Application();
        Application exampleDomain = new Application();

        when(mapper.map(exampleDomain, ApplicationEntity.class)).thenReturn(entity);
        when(repository.findAll(any(Example.class))).thenReturn(Flux.just(entity));
        when(mapper.map(entity, Application.class)).thenReturn(domain);

        // Act & Assert
        StepVerifier.create(repositoryAdapter.findByExample(exampleDomain))
                .expectNext(domain)
                .verifyComplete();
    }

    @Test
    void save_shouldReturnApplication_whenEntityIsPersisted() {
        // Arrange
        ApplicationEntity entity = new ApplicationEntity();
        Application domain = new Application();

        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.map(domain, ApplicationEntity.class)).thenReturn(entity);
        when(repository.save(any(ApplicationEntity.class))).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Application.class)).thenReturn(domain);

        // Act & Assert
        StepVerifier.create(repositoryAdapter.save(domain))
                .expectNext(domain)
                .verifyComplete();
    }

    @Test
    void findByFilter_shouldReturnPaginatedResponse_whenThereIsData() {
        ApplicationFilter filter = new ApplicationFilter(Optional.empty(), Optional.empty());
        PaginationParams params = new PaginationParams(1, 10);

        ApplicationDetails details = new ApplicationDetails("test@mail.com", 1000L, LocalDate.now(), "Vivienda", 12, States.APPROVED.getValue(), 100.0f);

        mockCount(1L);
        mockData(List.of(details));

        StepVerifier.create(repositoryAdapter.findByFilter(filter, params))
                .expectNextMatches(resp -> resp.totalElements() == 1L && !resp.data().isEmpty())
                .verifyComplete();
    }

    @Test
    void findByFilter_shouldReturnEmptyList_whenCountIsZero() {
        ApplicationFilter filter = new ApplicationFilter(Optional.empty(), Optional.of(true));
        PaginationParams params = new PaginationParams(1, 10);

        mockCount(0L);

        StepVerifier.create(repositoryAdapter.findByFilter(filter, params))
                .expectNextMatches(resp -> resp.totalElements() == 0L && resp.data().isEmpty())
                .verifyComplete();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockCount(long count) {
        DatabaseClient.GenericExecuteSpec specCount = mock(DatabaseClient.GenericExecuteSpec.class);
        RowsFetchSpec<Long> rowsFetchCount = mock(RowsFetchSpec.class);

        when(databaseClient.sql(startsWith("SELECT COUNT"))).thenReturn(specCount);
        when(specCount.map(any(BiFunction.class))).thenReturn(rowsFetchCount);
        when(rowsFetchCount.one()).thenReturn(Mono.just(count));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockData(List<ApplicationDetails> data) {
        DatabaseClient.GenericExecuteSpec specData = mock(DatabaseClient.GenericExecuteSpec.class);
        RowsFetchSpec<ApplicationDetails> rowsFetchData = mock(RowsFetchSpec.class);

        when(databaseClient.sql(startsWith("SELECT s.email"))).thenReturn(specData);
        when(specData.bind(eq("limit"), anyInt())).thenReturn(specData);
        when(specData.bind(eq("offset"), anyInt())).thenReturn(specData);
        when(specData.map(any(BiFunction.class))).thenReturn(rowsFetchData);
        when(rowsFetchData.all()).thenReturn(Flux.fromIterable(data));
    }

}
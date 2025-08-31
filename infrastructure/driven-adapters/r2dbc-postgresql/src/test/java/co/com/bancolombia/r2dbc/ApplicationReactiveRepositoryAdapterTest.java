package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.application.Application;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationReactiveRepositoryAdapterTest {

    @InjectMocks
    ApplicationReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    ApplicationReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Test
    void shouldFindApplicationById() {
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
    void shouldFindAllApplications() {
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
    void shouldFindApplicationsByExample() {
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
    void shouldSaveApplication() {
        // Arrange
        ApplicationEntity entity = new ApplicationEntity();
        Application domain = new Application();

        when(mapper.map(domain, ApplicationEntity.class)).thenReturn(entity);
        when(repository.save(any(ApplicationEntity.class))).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Application.class)).thenReturn(domain);

        // Act & Assert
        StepVerifier.create(repositoryAdapter.save(domain))
                .expectNext(domain)
                .verifyComplete();
    }
}
package co.com.bancolombia.r2dbc.typeloan;

import co.com.bancolombia.model.typeloan.TypeLoan;
import co.com.bancolombia.r2dbc.typeloan.entity.TypeLoanEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeLoanReactiveRepositoryAdapterTest {

    @InjectMocks
    TypeLoanReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    TypeLoanReactiveRepository repository;

    @Mock
    ObjectMapper mapper;
    @Test
    void mustFindByName() {
        // Arrange
        String loanName = "Préstamo de vivienda";

        TypeLoanEntity entity = TypeLoanEntity.builder()
                .id(UUID.randomUUID())
                .name(loanName)
                .minimumAmount(1000)
                .maximumAmount(100000)
                .interestRate(5.0)
                .automaticValidation(true)
                .build();

        TypeLoan domain = TypeLoan.builder()
                .id(entity.getId())
                .name(loanName)
                .minimumAmount(entity.getMinimumAmount())
                .maximumAmount(entity.getMaximumAmount())
                .interestRate(entity.getInterestRate())
                .automaticValidation(entity.getAutomaticValidation())
                .build();

        // Mockeamos el repositorio y el mapper
        when(repository.findByName(loanName)).thenReturn(Mono.just(domain));

        // Act
        Mono<TypeLoan> result = repositoryAdapter.findByName(loanName);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(tl -> tl.getName().equals(loanName) &&
                        tl.getMinimumAmount().equals(1000) &&
                        tl.getMaximumAmount().equals(100000))
                .verifyComplete();
    }
}
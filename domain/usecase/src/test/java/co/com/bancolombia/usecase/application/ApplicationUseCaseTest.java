package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.TransactionalOperatorGateway;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.typeloan.TypeLoan;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
import co.com.bancolombia.model.user.UserQueryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationUseCaseTest {

    @InjectMocks
    private ApplicationUseCase applicationUseCase;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private TypeLoanRepository typeLoanRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private TransactionalOperatorGateway transactionalOperatorGateway;

    @Mock
    private UserQueryGateway userQueryGateway;

    private final UUID typeLoanId = UUID.randomUUID();
    private final UUID stateId = UUID.randomUUID();
    private final Application testApplication = Application.builder()
            .amount(5000)
            .term(LocalDate.now())
            .email("test@mail.com")
            .document("123456789")
            .build();
    private final String typeLoanName = "Vivienda";

    @BeforeEach
    void setup() {
        given(transactionalOperatorGateway.execute(any(Mono.class))).willAnswer(invocation -> {
            Mono<?> mono = invocation.getArgument(0);
            return mono;
        });
    }

    @Test
    void givenUserExistsAndValidData_whenSaveApplication_thenShouldReturnApplication() {
        // Arrange
        TypeLoan mockTypeLoan = TypeLoan.builder().id(typeLoanId).name(typeLoanName).build();
        State mockState = State.builder().id(stateId).name("Pendiente de revisión").build();
        Application applicationWithIds = testApplication.toBuilder()
                .idTypeLoan(mockTypeLoan.getId())
                .idState(mockState.getId())
                .build();

        // Mock dependencies for the happy path
        given(userQueryGateway.existByDocument(any(String.class))).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.just(mockTypeLoan));
        given(stateRepository.save(any(State.class))).willReturn(Mono.just(mockState));
        given(applicationRepository.save(any(Application.class))).willReturn(Mono.just(applicationWithIds));

        // Act
        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName, testApplication.getDocument());

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(application -> application.getIdTypeLoan().equals(typeLoanId) &&
                        application.getIdState().equals(stateId))
                .verifyComplete();

        // Verify that all gateways were called
        verify(userQueryGateway).existByDocument(testApplication.getDocument());
        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository).save(any(State.class));
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void givenUserDoesNotExist_whenSaveApplication_thenShouldReturnBusinessException() {
        // Arrange
        given(userQueryGateway.existByDocument(any(String.class))).willReturn(Mono.just(false));

        // Act
        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName, testApplication.getDocument());

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                "User does not exist".equals(throwable.getMessage()))
                .verify();

        // Verify that no further gateways were called
        verify(userQueryGateway).existByDocument(testApplication.getDocument());
        verify(typeLoanRepository, never()).findByName(any());
        verify(stateRepository, never()).save(any());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void givenNonExistentTypeLoan_whenSaveApplication_thenShouldReturnBusinessException() {
        // Arrange
        given(userQueryGateway.existByDocument(any(String.class))).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.empty());

        // Act
        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName, testApplication.getDocument());

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        "B400-00".equals(((BusinessException) throwable).getCode()))
                .verify();

        // Verify that the flow stopped at typeLoanRepository
        verify(userQueryGateway).existByDocument(testApplication.getDocument());
        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository, never()).save(any());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void givenStateSaveFails_whenSaveApplication_thenShouldPropagateError() {
        // Arrange
        TypeLoan mockTypeLoan = TypeLoan.builder().id(typeLoanId).name(typeLoanName).build();
        given(userQueryGateway.existByDocument(any(String.class))).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.just(mockTypeLoan));
        given(stateRepository.save(any(State.class))).willReturn(Mono.error(new RuntimeException("DB connection failed")));

        // Act
        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName, testApplication.getDocument());

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        // Verify the flow stopped at stateRepository
        verify(userQueryGateway).existByDocument(testApplication.getDocument());
        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository).save(any(State.class));
        verify(applicationRepository, never()).save(any());
    }
}
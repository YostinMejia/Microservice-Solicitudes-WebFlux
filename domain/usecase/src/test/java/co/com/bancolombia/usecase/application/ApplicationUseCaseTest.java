package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.TransactionalOperatorGateway;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.typeloan.TypeLoan;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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
    void givenValidData_whenSaveApplication_thenShouldReturnApplication() {
        TypeLoan mockTypeLoan = TypeLoan.builder().id(typeLoanId).name(typeLoanName).build();
        State mockState = State.builder().id(stateId).name("Pendiente de revisión").build();
        Application applicationWithIds = testApplication.toBuilder()
                .idTypeLoan(mockTypeLoan.getId())
                .idState(mockState.getId())
                .build();

        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.just(mockTypeLoan));
        given(stateRepository.save(any(State.class))).willReturn(Mono.just(mockState));
        given(applicationRepository.save(any(Application.class))).willReturn(Mono.just(applicationWithIds));

        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName);

        StepVerifier.create(result)
                .expectNextMatches(application -> application.getIdTypeLoan().equals(typeLoanId) &&
                        application.getIdState().equals(stateId))
                .verifyComplete();

        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository).save(any(State.class));
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void givenNonExistentTypeLoan_whenSaveApplication_thenShouldReturnBusinessException() {
        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.empty());

        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        "B400-00".equals(((BusinessException) throwable).getCode()))
                .verify();

        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository, never()).save(any());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void givenStateSaveFails_whenSaveApplication_thenShouldPropagateError() {
        TypeLoan mockTypeLoan = TypeLoan.builder().id(typeLoanId).name(typeLoanName).build();
        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.just(mockTypeLoan));
        given(stateRepository.save(any(State.class))).willReturn(Mono.error(new RuntimeException("DB connection failed")));

        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository).save(any(State.class));
        verify(applicationRepository, never()).save(any());
    }
}
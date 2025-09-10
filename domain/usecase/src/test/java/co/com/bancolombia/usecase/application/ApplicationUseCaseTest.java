package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationDetails;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.auth.Role;
import co.com.bancolombia.model.auth.gateway.AuthGateway;
import co.com.bancolombia.model.dto.PaginationParams;
import co.com.bancolombia.model.dto.PaginationResponse;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.States;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.typeloan.TypeLoan;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
import co.com.bancolombia.model.user.UserGateway;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.usecase.state.StateUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    private UserGateway userGateway;

    @Mock
    private AuthGateway authGateway;

    @Mock
    private StateUseCase stateUseCase;

    private final UUID typeLoanId = UUID.randomUUID();
    private final UUID stateId = UUID.randomUUID();
    private final UUID applicationId = UUID.randomUUID();

    private final Application testApplication = Application.builder()
            .amount(5000)
            .term(LocalDate.now())
            .email("test@mail.com")
            .document("123456789")
            .build();
    private final String typeLoanName = "Vivienda";
    private final String authHeader = "Bearer token";
    private final String userEmail = "test@mail.com";
    private final String userDocument = "123456789";

    @BeforeEach
    void setup() {
    }

    @Test
    void givenUserExistsAndValidData_whenSaveApplication_thenShouldReturnApplication() {
        // Arrange
        given(authGateway.isSameEmailAsToken(userEmail, authHeader)).willReturn(Mono.just(true));
        TypeLoan mockTypeLoan = TypeLoan.builder().id(typeLoanId).name(typeLoanName).build();
        State mockState = State.builder().id(stateId).name("Pendiente de revisión").build();
        Application applicationWithIds = testApplication.toBuilder()
                .idTypeLoan(mockTypeLoan.getId())
                .idState(mockState.getId())
                .build();

        given(userGateway.existByDocumentAndEmail(userDocument, userEmail, authHeader)).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.just(mockTypeLoan));
        given(stateRepository.save(any(State.class))).willReturn(Mono.just(mockState));
        given(applicationRepository.save(any(Application.class))).willReturn(Mono.just(applicationWithIds));

        // Act
        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName, userDocument, userEmail, authHeader);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(application -> application.getIdTypeLoan().equals(typeLoanId) &&
                        application.getIdState().equals(stateId))
                .verifyComplete();

        verify(authGateway).isSameEmailAsToken(userEmail, authHeader);
        verify(userGateway).existByDocumentAndEmail(userDocument, userEmail, authHeader);
        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository).save(any(State.class));
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void givenUserDoesNotExist_whenSaveApplication_thenShouldReturnBusinessException() {
        // Arrange
        given(authGateway.isSameEmailAsToken(userEmail, authHeader)).willReturn(Mono.just(true));
        given(userGateway.existByDocumentAndEmail(userDocument, userEmail, authHeader)).willReturn(Mono.just(false));

        // Act
        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName, userDocument, userEmail, authHeader);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                ((BusinessException) throwable).getCode().equals(BusinessErrorCode.USER_NOT_FOUND.getBusinessCode()) &&
                                throwable.getMessage().equals(BusinessErrorCode.USER_NOT_FOUND.getMessage())
                )
                .verify();

        verify(authGateway).isSameEmailAsToken(userEmail, authHeader);
        verify(userGateway).existByDocumentAndEmail(userDocument, userEmail, authHeader);
        verify(typeLoanRepository, never()).findByName(any());
        verify(stateRepository, never()).save(any());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void givenNonExistentTypeLoan_whenSaveApplication_thenShouldReturnBusinessException() {
        // Arrange
        given(authGateway.isSameEmailAsToken(userEmail, authHeader)).willReturn(Mono.just(true));
        given(userGateway.existByDocumentAndEmail(userDocument, userEmail, authHeader)).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.empty());

        // Act
        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName, userDocument, userEmail, authHeader);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                ((BusinessException) throwable).getCode().equals(BusinessErrorCode.TYPE_LOAN_NOT_FOUND.getBusinessCode()) &&
                                throwable.getMessage().equals(BusinessErrorCode.TYPE_LOAN_NOT_FOUND.getMessage())
                )
                .verify();

        verify(authGateway).isSameEmailAsToken(userEmail, authHeader);
        verify(userGateway).existByDocumentAndEmail(userDocument, userEmail, authHeader);
        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository, never()).save(any());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void givenStateSaveFails_whenSaveApplication_thenShouldPropagateError() {
        // Arrange
        TypeLoan mockTypeLoan = TypeLoan.builder().id(typeLoanId).name(typeLoanName).build();
        given(authGateway.isSameEmailAsToken(userEmail, authHeader)).willReturn(Mono.just(true));
        given(userGateway.existByDocumentAndEmail(userDocument, userEmail, authHeader)).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(typeLoanName)).willReturn(Mono.just(mockTypeLoan));
        given(stateRepository.save(any(State.class))).willReturn(Mono.error(new RuntimeException("DB connection failed")));

        // Act
        Mono<Application> result = applicationUseCase.save(testApplication, typeLoanName, userDocument, userEmail, authHeader);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(authGateway).isSameEmailAsToken(userEmail, authHeader);
        verify(userGateway).existByDocumentAndEmail(userDocument, userEmail, authHeader);
        verify(typeLoanRepository).findByName(typeLoanName);
        verify(stateRepository).save(any(State.class));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void givenFindByFilter_whenUserIsUnauthorized_thenShouldReturnUnauthorized() {
        // Arrange
        given(authGateway.getRolByAuthHeaderToken(authHeader)).willReturn(Mono.empty());

        // Act
        Mono<PaginationResponse<ApplicationDetails>> result =
                applicationUseCase.findByFilter(new ApplicationFilter(Optional.empty(), Optional.empty()),
                        new PaginationParams(1, 10), authHeader);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                ((BusinessException) throwable).getCode().equals(BusinessErrorCode.UNAUTHORIZED_GET_LOAN_TYPE.getBusinessCode()) &&
                                throwable.getMessage().equals(BusinessErrorCode.UNAUTHORIZED_GET_LOAN_TYPE.getMessage())
                )
                .verify();

        verify(authGateway).getRolByAuthHeaderToken(authHeader);
        verify(applicationRepository, never()).findByFilter(any(), any());
    }

    @Test
    void givenFindByFilter_whenFilterMatch_thenShouldReturnPaginatedResponse() {
        // Arrange
        ApplicationDetails detail = new ApplicationDetails("test@mail.com", 5000L, LocalDate.now(), "Vivienda", 12, "Pendiente", 450.75f);
        PaginationResponse<ApplicationDetails> expectedResponse = new PaginationResponse<>("OK", "200", 1, 1L, List.of(detail));

        given(authGateway.getRolByAuthHeaderToken(authHeader)).willReturn(Mono.just(Role.ADMINISTRATOR.getValue()));
        given(applicationRepository.findByFilter(any(ApplicationFilter.class), any(PaginationParams.class)))
                .willReturn(Mono.just(expectedResponse));

        // Act
        Mono<PaginationResponse<ApplicationDetails>> result =
                applicationUseCase.findByFilter(new ApplicationFilter(Optional.empty(), Optional.empty()),
                        new PaginationParams(1, 10), authHeader);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.size() == 1 && response.data().get(0).email().equals("test@mail.com"))
                .verifyComplete();

        verify(authGateway).getRolByAuthHeaderToken(authHeader);
        verify(applicationRepository).findByFilter(any(), any());
    }

    @Test
    void givenUpdate_whenUserIsUnauthorized_thenShouldReturnUnauthorized() {
        // Arrange
        UUID applicationId = UUID.randomUUID();
        given(authGateway.getRolByAuthHeaderToken(any(String.class))).willReturn(Mono.empty());

        // Act
        Mono<Application> result = applicationUseCase.update(applicationId, States.APPROVED.getValue(), authHeader);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                ((BusinessException) throwable).getCode().equals(BusinessErrorCode.UNAUTHORIZED_UPDATE_STATE.getBusinessCode()) &&
                                throwable.getMessage().equals(BusinessErrorCode.UNAUTHORIZED_UPDATE_STATE.getMessage())
                )
                .verify();

        verify(authGateway).getRolByAuthHeaderToken(authHeader);
        verify(applicationRepository, never()).findById(any());
        verify(stateUseCase, never()).update(any(), any());
    }

    @Test
    void givenUpdate_whenApplicationDoesNotExist_thenShouldReturnError() {
        // Arrange
        UUID applicationId = UUID.randomUUID();
        given(authGateway.getRolByAuthHeaderToken(authHeader)).willReturn(Mono.just(Role.ADMINISTRATOR.getValue()));
        given(applicationRepository.findById(applicationId)).willReturn(Mono.empty());

        // Act
        Mono<Application> result = applicationUseCase.update(applicationId, States.APPROVED.getValue(), authHeader);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                ((BusinessException) throwable).getCode().equals(BusinessErrorCode.APPLICATION_LOAN_NOT_FOUND.getBusinessCode()) &&
                                throwable.getMessage().equals(BusinessErrorCode.APPLICATION_LOAN_NOT_FOUND.getMessage())
                )
                .verify();

        verify(authGateway).getRolByAuthHeaderToken(authHeader);
        verify(applicationRepository).findById(applicationId);
        verify(stateUseCase, never()).update(any(), any());
    }

    @Test
    void givenUpdate_whenParamsMatch_thenShouldUpdateApplicationState() {
        // Arrange
        UUID applicationId = UUID.randomUUID();
        UUID oldStateId = UUID.randomUUID();
        UUID newStateId = UUID.randomUUID();
        Application existingApplication = Application.builder().id(applicationId).idState(oldStateId).build();
        State updatedState = State.builder().id(newStateId).name(States.APPROVED.getValue()).build();

        given(authGateway.getRolByAuthHeaderToken(authHeader)).willReturn(Mono.just(Role.ADMINISTRATOR.getValue()));
        given(applicationRepository.findById(applicationId)).willReturn(Mono.just(existingApplication));
        given(stateUseCase.update(oldStateId, States.APPROVED.getValue())).willReturn(Mono.just(updatedState));

        // Act
        Mono<Application> result = applicationUseCase.update(applicationId, States.APPROVED.getValue(), authHeader);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getId().equals(applicationId) && app.getIdState().equals(newStateId))
                .verifyComplete();

        verify(authGateway).getRolByAuthHeaderToken(authHeader);
        verify(applicationRepository).findById(applicationId);
        verify(stateUseCase).update(oldStateId, States.APPROVED.getValue());
    }
}
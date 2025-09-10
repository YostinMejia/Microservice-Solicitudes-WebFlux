package co.com.bancolombia.api;

import co.com.bancolombia.api.application.ApplicationHandler;
import co.com.bancolombia.api.application.config.ApplicationPath;
import co.com.bancolombia.api.application.dto.ApplicationResponseDataDto;
import co.com.bancolombia.api.application.dto.CreateApplicationDto;
import co.com.bancolombia.api.application.mapper.ApplicationDtoMapper;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.auth.gateway.AuthGateway;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.typeloan.TypeLoan;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
import co.com.bancolombia.model.user.UserGateway;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import co.com.bancolombia.usecase.state.StateUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest
@ContextConfiguration(classes = {
        RouterRest.class,
        ApplicationHandler.class,
        ApplicationPath.class,
        ApplicationUseCase.class,
        RequestValidator.class,
        GlobalErrorWebExceptionHandler.class,
        StateUseCase.class,
})
@TestPropertySource(properties = {
        "routes.paths.applications.applications=/api/v1/solicitudes"
})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ApplicationPath applicationPath;

    @MockitoBean
    private ApplicationRepository applicationRepository;

    @MockitoBean
    private TypeLoanRepository typeLoanRepository;

    @MockitoBean
    private StateRepository stateRepository;

    @MockitoBean
    private UserGateway userGateway;

    @MockitoBean
    private AuthGateway authGateway;

    @MockitoBean
    private ApplicationDtoMapper applicationDtoMapper;

    @MockitoBean
    private RequestValidator requestValidator;
    private String authHeader;
    private UUID typeLoanId;
    private UUID stateId;
    private UUID appId;
    private CreateApplicationDto requestDto;

    @BeforeEach
    void setUp() {
        authHeader = "Bearer token";
        typeLoanId = UUID.randomUUID();
        stateId = UUID.randomUUID();
        appId = UUID.randomUUID();

        requestDto = new CreateApplicationDto(
                1000, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );

    }


    @Test
    void whenSuccess_shouldReturnCreatedAndHitRepository() {
        Application fakeApplication = Application.builder()
                .id(appId)
                .amount(requestDto.amount())
                .term(LocalDate.parse(requestDto.term()))
                .email(requestDto.email())
                .document(requestDto.document())
                .idState(stateId)
                .idTypeLoan(typeLoanId)
                .build();
        ApplicationResponseDataDto applicationResponseDataDto= new ApplicationResponseDataDto(fakeApplication.getAmount(),fakeApplication.getTerm().toString(),fakeApplication.getDocument(),fakeApplication.getEmail(),fakeApplication.getIdState(),fakeApplication.getIdTypeLoan());

        given(requestValidator.validator(any())).willReturn(Mono.just(requestDto));
        given(authGateway.isSameEmailAsToken(any(), any())).willReturn(Mono.just(true));
        given(userGateway.existByDocumentAndEmail(any(), any(), any())).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(any())).willReturn(Mono.just(TypeLoan.builder().id(typeLoanId).build()));
        given(stateRepository.save(any())).willReturn(Mono.just(State.builder().id(stateId).build()));
        given(applicationRepository.save(any())).willReturn(Mono.just(Application.builder().id(UUID.randomUUID()).build()));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(fakeApplication);
        given(applicationDtoMapper.toResponseData(any(Application.class))).willReturn(applicationResponseDataDto);

        webTestClient.post()
                .uri(applicationPath.getApplications())
                .header("Authorization", authHeader)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void whenValidationFails_shouldReturnBadRequest() {
        // Arrange
        given(requestValidator.validator(any()))
                .willReturn(Mono.error(new BusinessException(
                        List.of("amount must be > 0"),
                        BusinessErrorCode.VALIDATION_FAILED
                )));

        CreateApplicationDto invalidDto = new CreateApplicationDto(
                0, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );

        // Act & Assert
        webTestClient.post()
                .uri(applicationPath.getApplications())
                .bodyValue(invalidDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo(BusinessErrorCode.VALIDATION_FAILED.getMessage())
                .jsonPath("$.code").isEqualTo(BusinessErrorCode.VALIDATION_FAILED.getBusinessCode());
    }

    @Test
    void whenUserDoesNotExist_shouldReturnBadRequest() {
        // Arrange
        given(requestValidator.validator(any())).willReturn(Mono.just(requestDto));
        given(authGateway.isSameEmailAsToken(any(), any())).willReturn(Mono.just(true));
        given(userGateway.existByDocumentAndEmail(any(), any(), any())).willReturn(Mono.just(false));
        // Act & Assert
        webTestClient.post()
                .uri(applicationPath.getApplications())
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo(BusinessErrorCode.USER_NOT_FOUND.getMessage())
                .jsonPath("$.code").isEqualTo(BusinessErrorCode.USER_NOT_FOUND.getBusinessCode());
    }

    @Test
    void whenTypeLoanDoesNotExist_shouldReturnBadRequest() {
        // Arrange
        given(requestValidator.validator(any())).willReturn(Mono.just(requestDto));
        given(authGateway.isSameEmailAsToken(any(), any())).willReturn(Mono.just(true));
        given(userGateway.existByDocumentAndEmail(any(), any(), any())).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(any())).willReturn(Mono.empty());

        // Act & Assert
        webTestClient.post()
                .uri(applicationPath.getApplications())
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo(BusinessErrorCode.TYPE_LOAN_NOT_FOUND.getMessage())
                .jsonPath("$.code").isEqualTo(BusinessErrorCode.TYPE_LOAN_NOT_FOUND.getBusinessCode());
    }

    @Test
    void whenValidatorReturnsMultipleErrors_shouldReturnMultipleErrorsResponseDto() {
        // Arrange
        CreateApplicationDto invalidDto = new CreateApplicationDto(
                0, "bad-date", "Automóvil", "", "invalid-email"
        );

        given(requestValidator.validator(any()))
                .willReturn(Mono.error(new BusinessException(
                        List.of("amount must be > 0", "document required", "invalid email"),
                        BusinessErrorCode.VALIDATION_FAILED
                )));

        // Act & Assert
        webTestClient.post()
                .uri(applicationPath.getApplications())
                .bodyValue(invalidDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").isEqualTo("amount must be > 0")
                .jsonPath("$.errors[1]").isEqualTo("document required")
                .jsonPath("$.errors[2]").isEqualTo("invalid email")
                .jsonPath("$.message").isEqualTo(BusinessErrorCode.VALIDATION_FAILED.getMessage())
                .jsonPath("$.code").isEqualTo(BusinessErrorCode.VALIDATION_FAILED.getBusinessCode());
    }


}

package co.com.bancolombia.api;

import co.com.bancolombia.api.application.ApplicationHandler;
import co.com.bancolombia.api.application.config.ApplicationPath;
import co.com.bancolombia.api.application.dto.CreateApplicationDto;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.typeloan.TypeLoan;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
import co.com.bancolombia.model.user.UserGateway;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

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
        ApplicationDtoMapperImpl.class,
        RequestValidator.class,
        GlobalErrorWebExceptionHandler.class
})
@TestPropertySource(properties = {
        "routes.paths.applications.applications=/api/v1/solicitudes"
})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    // Mock external dependencies
    @MockitoBean
    private ApplicationRepository applicationRepository;

    @MockitoBean
    private TypeLoanRepository typeLoanRepository;

    @MockitoBean
    private StateRepository stateRepository;

    @MockitoBean
    private UserGateway userGateway;

    @MockitoBean
    private RequestValidator requestValidator; // 👈 lo mockeamos

    private final UUID typeLoanId = UUID.randomUUID();
    private final UUID stateId = UUID.randomUUID();
    private final CreateApplicationDto requestDto = new CreateApplicationDto(
            1000, "2025-12-01", "Automóvil", "12345", "test@example.com"
    );

    @Test
    void whenSuccess_shouldReturnCreatedAndHitRepository() {
        // Arrange
        given(requestValidator.validator(any())).willReturn(Mono.just(requestDto));
        given(userGateway.existByDocumentAndEmail(any(), )).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(any())).willReturn(Mono.just(TypeLoan.builder().id(typeLoanId).build()));
        given(stateRepository.save(any())).willReturn(Mono.just(State.builder().id(stateId).build()));
        given(applicationRepository.save(any())).willReturn(Mono.just(Application.builder().id(UUID.randomUUID()).build()));
        given(transactionalOperatorGateway.execute(any(Mono.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/solicitudes")
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
                .uri("/api/v1/solicitudes")
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
        given(userGateway.existByDocumentAndEmail(any(), )).willReturn(Mono.just(false));
        given(transactionalOperatorGateway.execute(any(Mono.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/solicitudes")
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
        given(userGateway.existByDocumentAndEmail(any(), )).willReturn(Mono.just(true));
        given(typeLoanRepository.findByName(any())).willReturn(Mono.empty());
        given(transactionalOperatorGateway.execute(any(Mono.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/solicitudes")
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
                .uri("/api/v1/solicitudes")
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

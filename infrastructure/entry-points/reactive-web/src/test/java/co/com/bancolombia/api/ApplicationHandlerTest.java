package co.com.bancolombia.api;

import co.com.bancolombia.api.application.ApplicationHandler;
import co.com.bancolombia.api.application.dto.CreateApplicationDto;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.api.application.mapper.ApplicationDtoMapper;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApplicationHandlerTest {

    @InjectMocks
    private ApplicationHandler applicationHandler;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private ApplicationDtoMapper applicationDtoMapper;

    @Mock
    private ApplicationUseCase applicationUseCase;

    private CreateApplicationDto requestDto;

    @BeforeEach
    void setUp() {

        requestDto = new CreateApplicationDto(
                1000, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );
    }

    @Test
    void listenPOSTApplication_whenSuccess_shouldReturnCreated() {
        // Arrange
        Application domainApplication = Application.builder().amount(1000).term(LocalDate.of(2025, 12, 1)).build();
        Application savedApplication = domainApplication.toBuilder().id(UUID.randomUUID()).build();

        // Mock dependencies to pass
        given(requestValidator.validator(any(CreateApplicationDto.class))).willReturn(Mono.just(requestDto));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(domainApplication);
        given(applicationUseCase.save(any(Application.class), any(String.class), any(String.class)))
                .willReturn(Mono.just(savedApplication));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        // Act & Assert
        StepVerifier.create(applicationHandler.listenPOSTApplication(serverRequest))
                .expectNextMatches(response -> response.statusCode() == HttpStatus.CREATED)
                .verifyComplete();
    }

    @Test
    void listenPOSTApplication_whenUserDoesNotExist_shouldReturnBadRequest() {
        // Arrange
        given(requestValidator.validator(any(CreateApplicationDto.class))).willReturn(Mono.just(requestDto));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(Application.builder().build());
        given(applicationUseCase.save(any(Application.class), any(String.class), any(String.class)))
                .willReturn(Mono.error(new BusinessException(BusinessErrorCode.USER_NOT_FOUND)));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        // Act & Assert
        StepVerifier.create(applicationHandler.listenPOSTApplication(serverRequest))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getCode().equals(BusinessErrorCode.USER_NOT_FOUND.getBusinessCode()))
                .verify();
    }

    @Test
    void listenPOSTApplication_whenValidationFails_shouldReturnBadRequest() {
        // Arrange
        CreateApplicationDto invalidDto = new CreateApplicationDto(
                -100, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );
        given(requestValidator.validator(any(CreateApplicationDto.class)))
                .willReturn(Mono.error(new BusinessException(
                        List.of("amount must be > 0"),
                        BusinessErrorCode.VALIDATION_FAILED
                )));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(invalidDto));

        // Act & Assert
        StepVerifier.create(applicationHandler.listenPOSTApplication(serverRequest))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getCode().equals(BusinessErrorCode.VALIDATION_FAILED.getBusinessCode()))
                .verify();
    }

    @Test
    void listenPOSTApplication_whenTypeLoanDoesNotExist_shouldReturnBadRequest() {
        // Arrange
        given(requestValidator.validator(any(CreateApplicationDto.class))).willReturn(Mono.just(requestDto));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(Application.builder().build());
        given(applicationUseCase.save(any(Application.class), any(String.class), any(String.class)))
                .willReturn(Mono.error(new BusinessException(BusinessErrorCode.TYPE_LOAN_NOT_FOUND)));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        // Act & Assert
        StepVerifier.create(applicationHandler.listenPOSTApplication(serverRequest))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException
                        && ((BusinessException) throwable).getCode().equals(BusinessErrorCode.TYPE_LOAN_NOT_FOUND.getBusinessCode()))
                .verify();
    }
}
package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CreateApplicationDto;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.api.mapper.ApplicationDtoMapper;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.dto.MultipleErrorsResponseDto;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
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
    private ApplicationUseCase applicationUseCase;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private ApplicationDtoMapper applicationDtoMapper;

    @Test
    void listenPOSTApplication_whenSuccess_shouldReturnCreated() {
        // Arrange
        CreateApplicationDto requestDto = new CreateApplicationDto(
                1000, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );
        Application domainApplication = Application.builder().amount(1000).term(LocalDate.of(2025, 12, 1)).build();
        Application savedApplication = domainApplication.toBuilder().id(UUID.randomUUID()).build();

        given(requestValidator.validator(any(CreateApplicationDto.class))).willReturn(Mono.error(new BusinessException(List.of("amount: debe ser mayor que o igual a 1"), "Create application validation failed", "B400-00")));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(domainApplication);
        given(applicationUseCase.save(any(Application.class), any(String.class))).willReturn(Mono.just(savedApplication));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        Mono<ServerResponse> responseMono = applicationHandler.listenPOSTApplication(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.CREATED)
                .verifyComplete();
    }

    @Test
    void listenPOSTApplication_whenValidationFails_shouldPropagateException() {
        // Arrange
        CreateApplicationDto invalidDto = new CreateApplicationDto(
                0, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );

        // Given that the RequestValidator throws a BusinessException
        given(requestValidator.validator(any(CreateApplicationDto.class)))
                .willReturn(Mono.error(new BusinessException(
                        List.of("amount: debe ser mayor que o igual a 1"),
                        "Create application validation failed",
                        "B400-00"
                )));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(invalidDto));

        // Act
        Mono<ServerResponse> responseMono = applicationHandler.listenPOSTApplication(serverRequest);

        // Assert
        // The handler should propagate the exception without handling it
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                "B400-00".equals(((BusinessException) throwable).getCode())
                )
                .verify();
    }

    @Test
    void listenPOSTApplication_whenUseCaseThrowsBusinessException_shouldReturnBadRequest() {
        // Arrange
        CreateApplicationDto requestDto = new CreateApplicationDto(
                1000, "2025-12-01", "Automóvil", "12345", "test@example.com"
        );
        Application domainApplication = Application.builder().amount(1000).term(LocalDate.of(2025, 12, 1)).build();

        // Mock dependencies to throw the exception
        given(requestValidator.validator(any(CreateApplicationDto.class))).willReturn(Mono.just(requestDto));
        given(applicationDtoMapper.toApplication(any(CreateApplicationDto.class))).willReturn(domainApplication);
        given(applicationUseCase.save(any(Application.class), any(String.class)))
                .willReturn(Mono.error(new BusinessException(null, "type of loan does not exist", "B400-00")));

        MockServerRequest serverRequest = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        // Act & Assert
        Mono<ServerResponse> responseMono = applicationHandler.listenPOSTApplication(serverRequest);

        // Expect the reactive stream to throw the exception
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof BusinessException)
                .verify();
    }
}
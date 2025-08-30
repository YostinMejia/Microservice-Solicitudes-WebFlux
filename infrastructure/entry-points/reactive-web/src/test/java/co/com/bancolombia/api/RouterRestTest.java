package co.com.bancolombia.api;

import co.com.bancolombia.api.config.ApplicationPath;
import co.com.bancolombia.api.dto.CreateApplicationDto;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.dto.MultipleErrorsResponseDto;
import co.com.bancolombia.model.dto.ResponseDto;
import co.com.bancolombia.model.dto.SingleErrorResponseDto;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ContextConfiguration(classes = {RouterRest.class, ApplicationHandler.class, ApplicationPath.class})
@WebFluxTest
@TestPropertySource(properties = {
        "routes.paths.applications.applications=/api/v1/solicitudes"
})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ApplicationUseCase applicationUseCase;

    @MockitoBean
    private ApplicationHandler applicationHandler;

    @BeforeEach
    void setup() {
        // You can use Mockito.reset(applicationHandler) here if needed
    }

    @Test
    void testListenPOSTUseCase_whenSuccess() {
        // Arrange
        CreateApplicationDto createApplicationDto = new CreateApplicationDto(
                1234,
                "2025-11-12",
                "Automóvil",
                "12345",
                "test@mail.com"
        );

        Application mockApplication = new Application().toBuilder()
                .amount(1234)
                .term(LocalDate.of(2025, 11, 12))
                .document("12345")
                .email("test@mail.com")
                .build();

        ResponseDto<Application> expectedResponse = new ResponseDto<>(
                "Application created successfully", "201-00", mockApplication
        );

        given(applicationHandler.listenPOSTApplication(any()))
                .willReturn(ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(expectedResponse)
                ); // Using block() to get ServerResponse directly for mocking

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createApplicationDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Application created successfully");
    }

    @Test
    void testListenPOSTUseCase_whenValidationFails() {
        // Arrange
        CreateApplicationDto invalidDto = new CreateApplicationDto(
                -100, // Invalid amount
                "2025-11-12",
                "Automóvil",
                "12345",
                "test@mail.com"
        );

        MultipleErrorsResponseDto responseBody = new MultipleErrorsResponseDto(
                List.of("amount: debe ser mayor a 0"),
                "Validation Failed",
                "B400-00"
        );

        given(applicationHandler.listenPOSTApplication(any()))
                .willReturn(ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseBody)
                );

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(MultipleErrorsResponseDto.class)
                .isEqualTo(responseBody);
    }

    @Test
    void testListenPOSTUseCase_whenInternalErrorOccurs() {
        // Arrange
        CreateApplicationDto validDto = new CreateApplicationDto(
                1000000,
                "2025-11-12",
                "Automóvil",
                "12345",
                "test@mail.com"
        );

        SingleErrorResponseDto responseBody = new SingleErrorResponseDto("Internal Server Error", "I500-00");

        given(applicationHandler.listenPOSTApplication(any()))
                .willReturn(ServerResponse.status(500)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseBody));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validDto)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(SingleErrorResponseDto.class)
                .isEqualTo(responseBody);
    }
}
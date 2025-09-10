package co.com.bancolombia.api.helper;

import co.com.bancolombia.api.authMicroservice.user.UserGatewayAdapter;
import co.com.bancolombia.api.authMicroservice.user.config.UserPath;
import co.com.bancolombia.model.dto.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGatewayAdapterTest {

    @InjectMocks
    private UserGatewayAdapter userGatewayAdapter;

    // Use @Mock for all dependencies for a consistent unit testing approach.
    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private UserPath userPath;

    private final String document = "123456789";
    private final String email = "test@example.com";
    private final String authHeader = "Bearer token";

    @BeforeEach
    void setup() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), eq(authHeader))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // 👇 Aquí está la clave: mockear onStatus para que NO devuelva null
        when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);

        when(userPath.getExistsByDocumentAndEmail()).thenReturn("/api/v1/usuarios/exists");
    }

    @Test
    void shouldReturnTrueWhenUserExists() {
        // Arrange
        Response<Boolean> response = new Response<>(null, null, true);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(response));

        // Act
        Mono<Boolean> result = userGatewayAdapter.existByDocumentAndEmail(document, email, authHeader);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExist() {
        // Arrange
        Response<Boolean> response = new Response<>(null, null, false);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(response));

        // Act
        Mono<Boolean> result = userGatewayAdapter.existByDocumentAndEmail(document, email, authHeader);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }
}
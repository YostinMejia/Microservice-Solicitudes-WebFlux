package co.com.bancolombia.api.helper;

import co.com.bancolombia.api.authMicroservice.user.UserGatewayAdapter;
import co.com.bancolombia.api.authMicroservice.user.config.UserPath;
import co.com.bancolombia.model.dto.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGatewayAdapterTest {

    @InjectMocks
    private UserGatewayAdapter userGatewayAdapter;

    // Use @Mock for all dependencies for a consistent unit testing approach.
    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private UserPath userPath;

    @BeforeEach
    void setup() {
        // Mock the WebClient call chain.
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class), any(String.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void shouldReturnTrueWhenUserExists() {
        // Arrange
        String document = "123456789";
        ResponseDto<Boolean> responseDto = new ResponseDto<>(null, null, true);

        when(userPath.getExistsByDocument()).thenReturn("/api/v1/usuarios/{document}");

        // Simulate WebClient returning a successful response.
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(responseDto));

        // Act
        Mono<Boolean> result = userGatewayAdapter.existByDocumentAndEmail(document, );

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExist() {
        // Arrange
        String document = "non-existent-user";
        ResponseDto<Boolean> responseDto = new ResponseDto<>(null, null, false);

        when(userPath.getExistsByDocument()).thenReturn("/api/v1/usuarios/{document}");

        // Simulate WebClient returning a response with false data.
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(responseDto));

        // Act
        Mono<Boolean> result = userGatewayAdapter.existByDocumentAndEmail(document, );

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }
}
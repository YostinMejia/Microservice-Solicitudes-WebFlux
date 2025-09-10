package co.com.bancolombia.api.authMicroservice.auth;

import co.com.bancolombia.api.authMicroservice.auth.config.AuthPath;
import co.com.bancolombia.api.authMicroservice.auth.dto.SameEmailAsTokenDto;
import co.com.bancolombia.model.exceptions.BusinessException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class AuthGatewayAdapterTest {

    private static MockWebServer mockWebServer;
    private AuthGatewayAdapter adapter;

    @BeforeAll
    static void setUpServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void init() {
        String baseUrl = mockWebServer.url("/").toString();

        AuthPath authPath = new AuthPath();
        authPath.setIsSameEmailAsToken(baseUrl + "is-same-email");
        authPath.setGetRoleByAuthHeaderToken(baseUrl + "get-role");

        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        adapter = new AuthGatewayAdapter(authPath, webClient);
    }

    @Test
    void whenIsSameEmail_success_shouldReturnTrue() {
        // Arrange: preparar respuesta mock
        String body = """
            {
              "data": true,
              "message": "ok",
              "code": "200"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        // Act + Assert
        StepVerifier.create(adapter.isSameEmailAsToken("test@mail.com", "Bearer token"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void whenGetRole_success_shouldReturnRole() {
        // Arrange
        String body = """
            {
              "data": "ADMIN",
              "message": "ok",
              "code": "200"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        // Act + Assert
        StepVerifier.create(adapter.getRolByAuthHeaderToken("Bearer token"))
                .expectNext("ADMIN")
                .verifyComplete();
    }

    @Test
    void whenUnauthorized_shouldThrowBusinessException() {
        // Arrange
        String body = """
            {
              "message": "Unauthorized",
              "code": "401"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        // Act + Assert
        StepVerifier.create(adapter.getRolByAuthHeaderToken("invalid"))
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getCode().equals("401") &&
                        ex.getMessage().equals("Unauthorized"))
                .verify();
    }
}

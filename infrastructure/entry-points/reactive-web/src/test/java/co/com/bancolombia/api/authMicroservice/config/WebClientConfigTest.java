package co.com.bancolombia.api.authMicroservice.config;

import static org.junit.jupiter.api.Assertions.*;


import co.com.bancolombia.api.application.config.ApplicationPath;

import co.com.bancolombia.api.authMicroservice.config.WebClientConfig;
import co.com.bancolombia.api.authMicroservice.user.config.UserPath;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ContextConfiguration(classes = {
        ApplicationPath.class
})
@TestPropertySource(properties = {
        "routes.paths.auth-microservice.baseUrl=http://localhost:8080"
})
@WebFluxTest
class WebClientConfigTest {

    @Mock
    private AuthMicroServicePath authMicroServicePath;

    @InjectMocks
    private WebClientConfig webClientConfig;

    @Test
    void webClientBeanIsCreated() {
        // Act
        WebClient webClient = webClientConfig.webClient();

        // Assert
        assertNotNull(webClient);
    }
}
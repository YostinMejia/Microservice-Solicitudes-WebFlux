package co.com.bancolombia.api.authMicroservice.config;

import co.com.bancolombia.api.authMicroservice.user.config.UserPath;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final AuthMicroServicePath authMicroServicePath;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(authMicroServicePath.getBaseUrl())
                .build();
    }
}

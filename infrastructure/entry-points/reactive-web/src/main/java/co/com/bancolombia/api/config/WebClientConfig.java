package co.com.bancolombia.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final UserPath userPath;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(userPath.getBaseUrl())
                .build();
    }
}

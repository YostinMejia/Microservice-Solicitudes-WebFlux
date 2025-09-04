package co.com.bancolombia.api.authMicroservice.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths.auth-microservice")
public class AuthMicroServicePath {
    private String baseUrl;
}
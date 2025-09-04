package co.com.bancolombia.api.authMicroservice.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths.auth-microservice.users")
public class UserPath {
    private String existsByDocumentAndEmail;
    private String baseUrl;
}

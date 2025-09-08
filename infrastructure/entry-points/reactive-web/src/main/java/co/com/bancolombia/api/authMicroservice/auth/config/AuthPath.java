package co.com.bancolombia.api.authMicroservice.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "routes.paths.auth-microservice.auth")
public class AuthPath {
    private String isSameEmailAsToken;
    private String getRoleByAuthHeaderToken;
}

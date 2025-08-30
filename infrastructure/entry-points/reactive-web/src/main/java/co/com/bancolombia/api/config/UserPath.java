package co.com.bancolombia.api.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths.users")

public class UserPath {
    private String existsByDocument;
    private String baseUrl;
}

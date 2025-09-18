package co.com.bancolombia.api.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths.applications")
public class ApplicationPath {
    private String applications;
    private String calculateDebtCapacity;
}

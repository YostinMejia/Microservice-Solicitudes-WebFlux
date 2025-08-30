package co.com.bancolombia.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservice Authentication ")
                        .version("1.0.0")
                        .description("API for user's authentication using WebFlux")
                        .contact(new Contact()
                                .name("Yostin Mejia")
                                .email("ysmaprogramming@gmail.com")));
    }
}
package co.com.bancolombia.config;

import co.com.bancolombia.model.auth.gateway.AuthGateway;
import co.com.bancolombia.model.user.UserGateway;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {

            // Get all bean definitions and check for a bean ending in 'UseCase'
            String[] beanNames = context.getBeanDefinitionNames();
            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    // You can also get the bean to verify its type
                    Object bean = context.getBean(beanName);
                    assertNotNull(bean);
                    // This is a more specific check for the ApplicationUseCase
                    assertTrue(bean instanceof ApplicationUseCase);
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'UseCase' were found in the context. Check @ComponentScan and the filter pattern.");
        }
    }

    // A minimal test configuration to load the UseCasesConfig and provide mocks for its dependencies
    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {
        // Since ApplicationUseCase has dependencies, we need to mock them to create a valid context.
        @Bean
        public co.com.bancolombia.model.application.gateways.ApplicationRepository applicationRepository() {
            return Mockito.mock(co.com.bancolombia.model.application.gateways.ApplicationRepository.class);
        }

        @Bean
        public co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository typeLoanRepository() {
            return Mockito.mock(co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository.class);
        }

        @Bean
        public co.com.bancolombia.model.state.gateways.StateRepository stateRepository() {
            return Mockito.mock(co.com.bancolombia.model.state.gateways.StateRepository.class);
        }

        @Bean
        public AuthGateway authGateway() {
            return Mockito.mock(AuthGateway.class);
        }


        @Bean
        public UserGateway userQueryGateway() {
            return Mockito.mock(UserGateway.class);
        }
    }
}
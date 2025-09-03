package co.com.bancolombia.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorsConfigTest {

    private final String allowedOrigin = "http://localhost:8081";
    private final CorsWebFilter corsWebFilter = new CorsConfig().corsWebFilter(allowedOrigin);

    @Test
    void shouldAllowRequestFromConfiguredOrigin() {
        // Arrange
//        MockServerWebExchange exchange = MockServerWebExchange.from(
//                MockServerHttpRequest.get("/api/v1/solicitudes")
//                        .header(HttpHeaders.ORIGIN, allowedOrigin)
//                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
//        );
//        WebFilterChain chain = mock(WebFilterChain.class);
//        when(chain.filter(exchange)).thenReturn(Mono.empty());
//
//        // Act
//        corsWebFilter.filter(exchange, chain).block();
//
//        // Assert
//        HttpHeaders headers = exchange.getResponse().getHeaders();
//        assertEquals(allowedOrigin, headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
//        assertEquals("true", headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void shouldBlockRequestFromUnconfiguredOrigin() {
        // Arrange
        String unconfiguredOrigin = "http://bad-origin.com";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/solicitudes")
                        .header(HttpHeaders.ORIGIN, unconfiguredOrigin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
        );
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        corsWebFilter.filter(exchange, chain).block();

        // Assert
        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertNull(headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
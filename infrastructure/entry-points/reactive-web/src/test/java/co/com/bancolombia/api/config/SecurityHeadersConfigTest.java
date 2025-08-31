package co.com.bancolombia.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityHeadersConfigTest {

    @Test
    void securityHeadersFilterAddsCorrectHeaders() {
        // Arrange
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));
        WebFilterChain chain = mock(WebFilterChain.class);

        // Mock the filter chain to return a completed Mono
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> filterMono = filter.filter(exchange, chain);
        filterMono.block();

        // Assert
        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertEquals("default-src 'self'; frame-ancestors 'self'; form-action 'self'", headers.getFirst("Content-Security-Policy"));
        assertEquals("max-age=31536000;", headers.getFirst("Strict-Transport-Security"));
        assertEquals("nosniff", headers.getFirst("X-Content-Type-Options"));
        assertEquals("", headers.getFirst("Server"));
        assertEquals("no-store", headers.getFirst("Cache-Control"));
        assertEquals("no-cache", headers.getFirst("Pragma"));
        assertEquals("strict-origin-when-cross-origin", headers.getFirst("Referrer-Policy"));
    }
}
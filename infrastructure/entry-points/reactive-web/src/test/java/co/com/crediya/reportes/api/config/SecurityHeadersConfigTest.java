package co.com.crediya.reportes.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersConfigTest {

    private static final String CSP_HEADER = "Content-Security-Policy";
    private static final String HSTS_HEADER = "Strict-Transport-Security";
    private static final String CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";
    private static final String SERVER_HEADER = "Server";
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";
    private static final String PRAGMA_HEADER = "Pragma";
    private static final String REFERRER_POLICY_HEADER = "Referrer-Policy";

    @Mock
    private WebFilterChain chain;

    private SecurityHeadersConfig securityHeadersConfig;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        securityHeadersConfig = new SecurityHeadersConfig();
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/reportes").build());
    }

    @Test
    void shouldAddAllSecurityHeaders() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = securityHeadersConfig.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        HttpHeaders headers = exchange.getResponse().getHeaders();
        
        // Verify all security headers are present
        assertTrue(headers.containsKey(CSP_HEADER));
        assertTrue(headers.containsKey(HSTS_HEADER));
        assertTrue(headers.containsKey(CONTENT_TYPE_OPTIONS_HEADER));
        assertTrue(headers.containsKey(SERVER_HEADER));
        assertTrue(headers.containsKey(CACHE_CONTROL_HEADER));
        assertTrue(headers.containsKey(PRAGMA_HEADER));
        assertTrue(headers.containsKey(REFERRER_POLICY_HEADER));

        verify(chain).filter(exchange);
    }

    @Test
    void shouldSetCorrectContentSecurityPolicyHeader() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(exchange, chain).block();

        // Then
        String cspValue = exchange.getResponse().getHeaders().getFirst(CSP_HEADER);
        assertEquals("default-src 'self'; frame-ancestors 'self'; form-action 'self'", cspValue);
    }

    @Test
    void shouldSetCorrectStrictTransportSecurityHeader() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(exchange, chain).block();

        // Then
        String hstsValue = exchange.getResponse().getHeaders().getFirst(HSTS_HEADER);
        assertEquals("max-age=31536000;", hstsValue);
    }

    @Test
    void shouldSetCorrectContentTypeOptionsHeader() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(exchange, chain).block();

        // Then
        String contentTypeValue = exchange.getResponse().getHeaders().getFirst(CONTENT_TYPE_OPTIONS_HEADER);
        assertEquals("nosniff", contentTypeValue);
    }

    @Test
    void shouldSetEmptyServerHeader() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(exchange, chain).block();

        // Then
        String serverValue = exchange.getResponse().getHeaders().getFirst(SERVER_HEADER);
        assertEquals("", serverValue);
    }

    @Test
    void shouldSetCorrectCacheControlHeader() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(exchange, chain).block();

        // Then
        String cacheControlValue = exchange.getResponse().getHeaders().getFirst(CACHE_CONTROL_HEADER);
        assertEquals("no-store", cacheControlValue);
    }

    @Test
    void shouldSetCorrectPragmaHeader() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(exchange, chain).block();

        // Then
        String pragmaValue = exchange.getResponse().getHeaders().getFirst(PRAGMA_HEADER);
        assertEquals("no-cache", pragmaValue);
    }

    @Test
    void shouldSetCorrectReferrerPolicyHeader() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(exchange, chain).block();

        // Then
        String referrerPolicyValue = exchange.getResponse().getHeaders().getFirst(REFERRER_POLICY_HEADER);
        assertEquals("strict-origin-when-cross-origin", referrerPolicyValue);
    }

    @Test
    void shouldContinueFilterChain() {
        // Given
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = securityHeadersConfig.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldHandleChainError() {
        // Given
        RuntimeException chainError = new RuntimeException("Chain error");
        when(chain.filter(exchange)).thenReturn(Mono.error(chainError));

        // When
        Mono<Void> result = securityHeadersConfig.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        // Headers should still be set even if chain fails
        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertTrue(headers.containsKey(CSP_HEADER));
        assertTrue(headers.containsKey(HSTS_HEADER));
    }

    @Test
    void shouldWorkWithDifferentRequestPaths() {
        // Given
        ServerWebExchange healthExchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/actuator/health").build()
        );
        when(chain.filter(healthExchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(healthExchange, chain).block();

        // Then
        HttpHeaders headers = healthExchange.getResponse().getHeaders();
        assertTrue(headers.containsKey(CSP_HEADER));
        assertTrue(headers.containsKey(HSTS_HEADER));
        assertTrue(headers.containsKey(CONTENT_TYPE_OPTIONS_HEADER));
    }

    @Test
    void shouldWorkWithPostRequests() {
        // Given
        ServerWebExchange postExchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/reportes/send-now").build()
        );
        when(chain.filter(postExchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(postExchange, chain).block();

        // Then
        HttpHeaders headers = postExchange.getResponse().getHeaders();
        assertTrue(headers.containsKey(CSP_HEADER));
        assertTrue(headers.containsKey(CACHE_CONTROL_HEADER));
        assertTrue(headers.containsKey(PRAGMA_HEADER));
    }

    @Test
    void shouldNotOverrideExistingHeaders() {
        // Given
        exchange.getResponse().getHeaders().set(SERVER_HEADER, "existing-server");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        securityHeadersConfig.filter(exchange, chain).block();

        // Then
        String serverValue = exchange.getResponse().getHeaders().getFirst(SERVER_HEADER);
        assertEquals("", serverValue); // Should override with empty string
    }
}

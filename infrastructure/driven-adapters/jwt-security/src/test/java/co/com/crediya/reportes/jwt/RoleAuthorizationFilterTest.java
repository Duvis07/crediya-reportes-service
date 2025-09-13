package co.com.crediya.reportes.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleAuthorizationFilterTest {

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    @Mock
    private WebFilterChain chain;

    private RoleAuthorizationFilter roleAuthorizationFilter;

    @BeforeEach
    void setUp() {
        roleAuthorizationFilter = new RoleAuthorizationFilter(errorResponseBuilder);
    }

    @Test
    void shouldAllowAccessForAdminRole() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes").build()
        );
        exchange.getAttributes().put("userRole", "ADMIN");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(errorResponseBuilder);
    }

    @Test
    void shouldDenyAccessForAsesorRole() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes").build()
        );
        exchange.getAttributes().put("userRole", "ASESOR");
        when(errorResponseBuilder.buildForbiddenResponse(eq(exchange), any(String.class)))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(errorResponseBuilder).buildForbiddenResponse(eq(exchange), eq("Access denied. ADMIN role required"));
        verifyNoInteractions(chain);
    }

    @Test
    void shouldDenyAccessWhenNoRolePresent() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes").build()
        );
        // No userRole attribute set
        when(errorResponseBuilder.buildForbiddenResponse(eq(exchange), any(String.class)))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(errorResponseBuilder).buildForbiddenResponse(eq(exchange), eq("Access denied. ADMIN role required"));
        verifyNoInteractions(chain);
    }

    @Test
    void shouldSkipValidationForNonReportesEndpoints() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/other").build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(errorResponseBuilder);
    }

    @Test
    void shouldSkipValidationForNonGetMethods() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/reportes").build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(errorResponseBuilder);
    }

    @Test
    void shouldValidateSubpathsOfReportesEndpoint() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes/summary").build()
        );
        exchange.getAttributes().put("userRole", "ADMIN");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(errorResponseBuilder);
    }

    @Test
    void shouldDenyAccessForInvalidRole() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes").build()
        );
        exchange.getAttributes().put("userRole", "INVALID_ROLE");
        when(errorResponseBuilder.buildForbiddenResponse(eq(exchange), any(String.class)))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(errorResponseBuilder).buildForbiddenResponse(eq(exchange), eq("Access denied. ADMIN role required"));
        verifyNoInteractions(chain);
    }

    @Test
    void shouldSkipValidationForPublicEndpoints() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/actuator/health").build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(errorResponseBuilder);
    }

    @Test
    void shouldAllowPutMethodWithoutValidation() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.put("/api/v1/reportes").build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = roleAuthorizationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(errorResponseBuilder);
    }
}

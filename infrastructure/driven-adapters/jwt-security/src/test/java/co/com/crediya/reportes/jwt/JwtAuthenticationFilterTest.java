package co.com.crediya.reportes.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private WebFilterChain chain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private SecretKey secretKey;
    private static final String JWT_SECRET = "mySecretKeyForTestingPurposesOnly123456789";

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        jwtAuthenticationFilter = new JwtAuthenticationFilter(JWT_SECRET, jwtUtils);
    }

    @Test
    void shouldAllowPublicEndpoints() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/actuator/health").build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void shouldAllowSwaggerEndpoints() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/swagger-ui/index.html").build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void shouldRejectRequestWithoutAuthorizationHeader() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes").build()
        );
        when(jwtUtils.unauthorized(eq(exchange), any(String.class)))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtils).unauthorized(eq(exchange), eq("Missing or invalid Authorization header"));
        verifyNoInteractions(chain);
    }

    @Test
    void shouldRejectRequestWithInvalidAuthorizationHeader() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes")
                    .header(HttpHeaders.AUTHORIZATION, "Invalid token")
                    .build()
        );
        when(jwtUtils.unauthorized(eq(exchange), any(String.class)))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtils).unauthorized(eq(exchange), eq("Missing or invalid Authorization header"));
        verifyNoInteractions(chain);
    }

    @Test
    void shouldProcessValidJwtToken() {
        // Given
        String validToken = createValidToken("user123", "ADMIN");
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(any(ServerWebExchange.class));
        
        // Verify user attributes were set
        assertEquals("user123", exchange.getAttributes().get("userId"));
        assertEquals("ADMIN", exchange.getAttributes().get("userRole"));
    }

    @Test
    void shouldHandleExpiredToken() {
        // Given
        String expiredToken = createExpiredToken("user123", "ADMIN");
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                    .build()
        );
        when(jwtUtils.isJwtRelatedError(any(ExpiredJwtException.class))).thenReturn(true);
        when(jwtUtils.getJwtErrorMessage(any(ExpiredJwtException.class))).thenReturn("Token has expired");
        when(jwtUtils.unauthorized(eq(exchange), eq("Token has expired")))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtils).unauthorized(eq(exchange), eq("Token has expired"));
        verifyNoInteractions(chain);
    }

    @Test
    void shouldHandleMalformedToken() {
        // Given
        String malformedToken = "invalid.jwt.token";
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + malformedToken)
                    .build()
        );
        when(jwtUtils.isJwtRelatedError(any(MalformedJwtException.class))).thenReturn(true);
        when(jwtUtils.getJwtErrorMessage(any(MalformedJwtException.class))).thenReturn("Invalid token format");
        when(jwtUtils.unauthorized(eq(exchange), eq("Invalid token format")))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtils).unauthorized(eq(exchange), eq("Invalid token format"));
        verifyNoInteractions(chain);
    }

    @Test
    void shouldPropagateNonJwtErrors() {
        // Given
        RuntimeException businessError = new RuntimeException("Business logic error");
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + createValidToken("user123", "ADMIN"))
                    .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.error(businessError));
        when(jwtUtils.isJwtRelatedError(businessError)).thenReturn(false);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldSetCorrectAuthorities() {
        // Given
        String validToken = createValidToken("user123", "ASESOR");
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/reportes")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals("ASESOR", exchange.getAttributes().get("userRole"));
    }

    private String createValidToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(secretKey)
                .compact();
    }

    private String createExpiredToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(secretKey)
                .compact();
    }
}

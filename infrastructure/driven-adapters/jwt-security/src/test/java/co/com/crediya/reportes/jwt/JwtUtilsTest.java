package co.com.crediya.reportes.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    private JwtUtils jwtUtils;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(errorResponseBuilder);
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
    }

    @Test
    void shouldDelegateUnauthorizedToErrorResponseBuilder() {
        // Given
        String message = "Invalid token";
        when(errorResponseBuilder.buildUnauthorizedResponse(any(ServerWebExchange.class), eq(message)))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtUtils.unauthorized(exchange, message);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldIdentifyJwtExceptionAsJwtRelatedError() {
        // Given
        JwtException error = new JwtException("JWT error");

        // When
        boolean result = jwtUtils.isJwtRelatedError(error);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldIdentifyExpiredJwtExceptionAsJwtRelatedError() {
        // Given
        ExpiredJwtException error = new ExpiredJwtException(null, null, "Token expired");

        // When
        boolean result = jwtUtils.isJwtRelatedError(error);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldIdentifyIllegalArgumentExceptionAsJwtRelatedError() {
        // Given
        IllegalArgumentException error = new IllegalArgumentException("Token is null");

        // When
        boolean result = jwtUtils.isJwtRelatedError(error);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldNotIdentifyRuntimeExceptionAsJwtRelatedError() {
        // Given
        RuntimeException error = new RuntimeException("Some other error");

        // When
        boolean result = jwtUtils.isJwtRelatedError(error);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnCorrectMessageForExpiredJwtException() {
        // Given
        ExpiredJwtException error = new ExpiredJwtException(null, null, "Token expired");

        // When
        String message = jwtUtils.getJwtErrorMessage(error);

        // Then
        assertEquals("Token has expired", message);
    }

    @Test
    void shouldReturnCorrectMessageForMalformedJwtException() {
        // Given
        MalformedJwtException error = new MalformedJwtException("Invalid format");

        // When
        String message = jwtUtils.getJwtErrorMessage(error);

        // Then
        assertEquals("Invalid token format", message);
    }

    @Test
    void shouldReturnCorrectMessageForUnsupportedJwtException() {
        // Given
        UnsupportedJwtException error = new UnsupportedJwtException("Unsupported");

        // When
        String message = jwtUtils.getJwtErrorMessage(error);

        // Then
        assertEquals("Unsupported token", message);
    }

    @Test
    void shouldReturnCorrectMessageForSignatureException() {
        // Given
        SignatureException error = new SignatureException("Invalid signature");

        // When
        String message = jwtUtils.getJwtErrorMessage(error);

        // Then
        assertEquals("Invalid token signature", message);
    }

    @Test
    void shouldReturnCorrectMessageForIllegalArgumentException() {
        // Given
        IllegalArgumentException error = new IllegalArgumentException("Token is null");

        // When
        String message = jwtUtils.getJwtErrorMessage(error);

        // Then
        assertEquals("Token is null or empty", message);
    }

    @Test
    void shouldReturnDefaultMessageForUnknownException() {
        // Given
        RuntimeException error = new RuntimeException("Unknown error");

        // When
        String message = jwtUtils.getJwtErrorMessage(error);

        // Then
        assertEquals("Invalid token", message);
    }
}

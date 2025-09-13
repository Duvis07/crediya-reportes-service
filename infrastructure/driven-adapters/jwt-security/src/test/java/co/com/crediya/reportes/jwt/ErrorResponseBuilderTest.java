package co.com.crediya.reportes.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ErrorResponseBuilderTest {

    private static final String TEST_PATH = "/test";
    
    private ErrorResponseBuilder errorResponseBuilder;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        errorResponseBuilder = new ErrorResponseBuilder();
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get(TEST_PATH).build());
    }

    @Test
    void shouldBuildForbiddenResponse() {
        // Given
        String message = "Access denied";

        // When
        Mono<Void> result = errorResponseBuilder.buildForbiddenResponse(exchange, message);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify response status and headers
        assert exchange.getResponse().getStatusCode() == HttpStatus.FORBIDDEN;
        assert exchange.getResponse().getHeaders().getContentType().toString().contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldBuildUnauthorizedResponse() {
        // Given
        String message = "Invalid token";

        // When
        Mono<Void> result = errorResponseBuilder.buildUnauthorizedResponse(exchange, message);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify response status and headers
        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
        assert exchange.getResponse().getHeaders().getContentType().toString().contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldBuildCorrectJsonForForbidden() {
        // Given
        String message = "Role not allowed";
        MockServerHttpResponse response = new MockServerHttpResponse(new DefaultDataBufferFactory());
        ServerWebExchange mockExchange = MockServerWebExchange.from(
            MockServerHttpRequest.get(TEST_PATH).build()
        ).mutate().response(response).build();

        // When
        Mono<Void> result = errorResponseBuilder.buildForbiddenResponse(mockExchange, message);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assert response.getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    void shouldBuildCorrectJsonForUnauthorized() {
        // Given
        String message = "Token expired";
        MockServerHttpResponse response = new MockServerHttpResponse(new DefaultDataBufferFactory());
        ServerWebExchange mockExchange = MockServerWebExchange.from(
            MockServerHttpRequest.get(TEST_PATH).build()
        ).mutate().response(response).build();

        // When
        Mono<Void> result = errorResponseBuilder.buildUnauthorizedResponse(mockExchange, message);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void shouldHandleEmptyMessage() {
        // Given
        String message = "";

        // When
        Mono<Void> result = errorResponseBuilder.buildForbiddenResponse(exchange, message);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    void shouldHandleNullMessage() {
        // Given
        String message = null;

        // When
        Mono<Void> result = errorResponseBuilder.buildUnauthorizedResponse(exchange, message);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }
}

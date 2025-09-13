package co.com.crediya.reportes.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class ErrorResponseBuilder {

    public Mono<Void> buildForbiddenResponse(ServerWebExchange exchange, String message) {
        return buildErrorResponse(exchange, HttpStatus.FORBIDDEN, "Forbidden", message);
    }

    public Mono<Void> buildUnauthorizedResponse(ServerWebExchange exchange, String message) {
        return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", message);
    }

    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, HttpStatus status, String error, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = String.format(
            "{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}", 
            error, message, status.value()
        );
        
        DataBuffer buffer = exchange.getResponse().bufferFactory()
            .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}

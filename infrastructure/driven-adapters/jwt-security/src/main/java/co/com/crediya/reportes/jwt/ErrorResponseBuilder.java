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
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = String.format(
            "{\"error\":\"Forbidden\",\"message\":\"%s\",\"status\":403}", 
            message
        );
        
        DataBuffer buffer = exchange.getResponse().bufferFactory()
            .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public Mono<Void> buildUnauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = String.format(
            "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}", 
            message
        );
        
        DataBuffer buffer = exchange.getResponse().bufferFactory()
            .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}

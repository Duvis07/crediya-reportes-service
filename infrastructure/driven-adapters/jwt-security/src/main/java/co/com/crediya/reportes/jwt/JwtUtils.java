package co.com.crediya.reportes.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    public Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
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

    public boolean isJwtRelatedError(Throwable error) {
        return error instanceof JwtException ||
               error instanceof ExpiredJwtException ||
               error instanceof MalformedJwtException ||
               error instanceof UnsupportedJwtException ||
               error instanceof SignatureException ||
               error instanceof IllegalArgumentException;
    }

    public String getJwtErrorMessage(Throwable error) {
        if (error instanceof ExpiredJwtException) {
            return "Token has expired";
        } else if (error instanceof MalformedJwtException) {
            return "Invalid token format";
        } else if (error instanceof UnsupportedJwtException) {
            return "Unsupported token";
        } else if (error instanceof SignatureException) {
            return "Invalid token signature";
        } else if (error instanceof IllegalArgumentException) {
            return "Token is null or empty";
        } else {
            return "Invalid token";
        }
    }
}

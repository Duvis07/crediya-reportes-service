package co.com.crediya.reportes.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    private final ErrorResponseBuilder errorResponseBuilder;

    public Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return errorResponseBuilder.buildUnauthorizedResponse(exchange, message);
    }

    public boolean isJwtRelatedError(Throwable error) {
        return error instanceof JwtException ||
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

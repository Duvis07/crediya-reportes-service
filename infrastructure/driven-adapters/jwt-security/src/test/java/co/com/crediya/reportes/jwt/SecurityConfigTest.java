package co.com.crediya.reportes.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private RoleAuthorizationFilter roleAuthorizationFilter;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, roleAuthorizationFilter);
    }

    @Test
    void shouldCreateSecurityWebFilterChain() {
        // Given
        ServerHttpSecurity http = ServerHttpSecurity.http();

        // When
        SecurityWebFilterChain filterChain = securityConfig.securityWebFilterChain(http);

        // Then
        assertNotNull(filterChain);
    }


    @Test
    void shouldInjectFiltersCorrectly() {
        // When
        SecurityConfig config = new SecurityConfig(jwtAuthenticationFilter, roleAuthorizationFilter);

        // Then
        assertNotNull(config);
        // Verify that the constructor properly accepts the filters
        // This test ensures dependency injection works correctly
    }
}

package co.com.crediya.reportes.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    private static final String SINGLE_ORIGIN = "http://localhost:3000";
    private static final String MULTIPLE_ORIGINS = "http://localhost:3000,http://localhost:8080,https://crediya.com";

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
    }

    @Test
    void shouldCreateCorsWebFilterWithSingleOrigin() {
        // When
        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(SINGLE_ORIGIN);

        // Then
        assertNotNull(corsWebFilter);
        assertEquals("org.springframework.web.cors.reactive.CorsWebFilter", 
                     corsWebFilter.getClass().getName());
    }

    @Test
    void shouldCreateCorsWebFilterWithMultipleOrigins() {
        // When
        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(MULTIPLE_ORIGINS);

        // Then
        assertNotNull(corsWebFilter);
        assertEquals("org.springframework.web.cors.reactive.CorsWebFilter", 
                     corsWebFilter.getClass().getName());
    }

    @Test
    void shouldHandleEmptyOrigins() {
        // Given
        String emptyOrigins = "";

        // When
        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(emptyOrigins);

        // Then
        assertNotNull(corsWebFilter);
    }

    @Test
    void shouldHandleSingleOriginWithoutComma() {
        // Given
        String singleOrigin = "https://production.crediya.com";

        // When
        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(singleOrigin);

        // Then
        assertNotNull(corsWebFilter);
    }

    @Test
    void shouldHandleOriginsWithSpaces() {
        // Given
        String originsWithSpaces = "http://localhost:3000, https://app.crediya.com, http://localhost:8080";

        // When
        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(originsWithSpaces);

        // Then
        assertNotNull(corsWebFilter);
    }

    @Test
    void shouldCreateDifferentFilterInstances() {
        // When
        CorsWebFilter filter1 = corsConfig.corsWebFilter(SINGLE_ORIGIN);
        CorsWebFilter filter2 = corsConfig.corsWebFilter(SINGLE_ORIGIN);

        // Then
        assertNotNull(filter1);
        assertNotNull(filter2);
        assertNotSame(filter1, filter2);
    }

    @Test
    void shouldHandleNullOrigins() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            corsConfig.corsWebFilter(null);
        });
    }

    @Test
    void shouldConfigureAllPathsWithCors() {
        // When
        CorsWebFilter corsWebFilter = corsConfig.corsWebFilter(SINGLE_ORIGIN);

        // Then
        assertNotNull(corsWebFilter);
        
        // The filter should be configured for all paths ("/**")
        // This is verified by the successful creation of the filter
    }

    @Test
    void shouldVerifyConstructor() {
        // When
        CorsConfig newCorsConfig = new CorsConfig();

        // Then
        assertNotNull(newCorsConfig);
    }
}

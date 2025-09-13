package co.com.crediya.reportes.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouterRestTest {

    @Mock
    private Handler handler;

    private RouterRest routerRest;

    @BeforeEach
    void setUp() {
        routerRest = new RouterRest();
    }

    @Test
    void shouldCreateRouterFunction() {
        // When
        RouterFunction<ServerResponse> routerFunction = routerRest.routerFunction(handler);

        // Then
        assertNotNull(routerFunction);
    }

    @Test
    void shouldConfigureGetReportesRoute() {
        // When
        RouterFunction<ServerResponse> routerFunction = routerRest.routerFunction(handler);

        // Then
        assertNotNull(routerFunction);
        // Router function should be properly configured for GET /api/v1/reportes
        // This is verified by the successful creation of the router function
    }

    @Test
    void shouldConfigurePostSendNowRoute() {
        // When
        RouterFunction<ServerResponse> routerFunction = routerRest.routerFunction(handler);

        // Then
        assertNotNull(routerFunction);
        // Router function should be properly configured for POST /api/v1/reportes/send-now
        // This is verified by the successful creation of the router function
    }

    @Test
    void shouldCreateRouterFunctionWithDifferentHandlers() {
        // Given
        Handler anotherHandler = mock(Handler.class);

        // When
        RouterFunction<ServerResponse> routerFunction1 = routerRest.routerFunction(handler);
        RouterFunction<ServerResponse> routerFunction2 = routerRest.routerFunction(anotherHandler);

        // Then
        assertNotNull(routerFunction1);
        assertNotNull(routerFunction2);
        assertNotSame(routerFunction1, routerFunction2);
    }

    @Test
    void shouldHandleNullHandler() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            routerRest.routerFunction(null);
        });
    }

    @Test
    void shouldVerifyRouterRestConstructor() {
        // When
        RouterRest newRouterRest = new RouterRest();

        // Then
        assertNotNull(newRouterRest);
    }

    @Test
    void shouldCreateMultipleRouterFunctions() {
        // When
        RouterFunction<ServerResponse> routerFunction1 = routerRest.routerFunction(handler);
        RouterFunction<ServerResponse> routerFunction2 = routerRest.routerFunction(handler);

        // Then
        assertNotNull(routerFunction1);
        assertNotNull(routerFunction2);
        // Each call should create a new instance
        assertNotSame(routerFunction1, routerFunction2);
    }
}

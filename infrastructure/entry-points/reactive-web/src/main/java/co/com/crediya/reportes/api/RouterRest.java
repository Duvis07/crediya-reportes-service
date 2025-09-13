package co.com.crediya.reportes.api;

import co.com.crediya.reportes.model.LoanReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/reportes",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "getLoanReports",
                            summary = "Obtener reporte de préstamos aprobados",
                            description = "Recupera el reporte consolidado con la cantidad total de préstamos aprobados y el monto total",
                            tags = {"Reportes"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Reporte recuperado exitosamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = LoanReport.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "401", description = "No autorizado - Se requiere autenticación"),
                                    @ApiResponse(responseCode = "403", description = "Prohibido - Solo asesores pueden acceder"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/reportes/send-now",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "sendDailyReport",
                            summary = "Enviar reporte diario manualmente",
                            description = "Dispara manualmente el envío del reporte diario de negocio por email a los administradores",
                            tags = {"Reportes"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Reporte enviado exitosamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(type = "string", example = "Daily business report sent successfully")
                                            )
                                    ),
                                    @ApiResponse(responseCode = "401", description = "No autorizado - Se requiere autenticación"),
                                    @ApiResponse(responseCode = "403", description = "Prohibido - Solo asesores pueden enviar reportes"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor - Fallo en el servicio de email")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET("/api/v1/reportes")
                        .and(accept(MediaType.APPLICATION_JSON)),
                handler::getLoanReports)
                .andRoute(POST("/api/v1/reportes/send-now")
                                .and(accept(MediaType.APPLICATION_JSON)),
                        handler::sendTestReport);
    }
}

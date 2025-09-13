package co.com.crediya.reportes.api;

import co.com.crediya.reportes.model.LoanReport;
import co.com.crediya.reportes.usecase.GetLoanReportUseCase;
import co.com.crediya.reportes.usecase.SendDailyReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@ExtendWith(MockitoExtension.class)
class HandlerTest {

    @Mock
    private GetLoanReportUseCase getLoanReportUseCase;

    @Mock
    private SendDailyReportUseCase sendDailyReportUseCase;

    private Handler handler;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        handler = new Handler(getLoanReportUseCase, sendDailyReportUseCase);
        
        RouterFunction<ServerResponse> routerFunction = RouterFunctions
                .route(GET("/api/v1/reportes"), handler::getLoanReports)
                .andRoute(POST("/api/v1/reportes/send-now"), handler::sendTestReport);
        
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void shouldGetLoanReportsSuccessfully() {
        // Given
        LoanReport expectedReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(15L)
                .totalApprovedAmount(BigDecimal.valueOf(750000))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(getLoanReportUseCase.getLoanReport()).thenReturn(Mono.just(expectedReport));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/reportes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(LoanReport.class)
                .isEqualTo(expectedReport);

        verify(getLoanReportUseCase, times(1)).getLoanReport();
    }

    @Test
    void shouldHandleGetLoanReportsError() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database connection failed");
        when(getLoanReportUseCase.getLoanReport()).thenReturn(Mono.error(expectedError));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/reportes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(body -> body.contains("Error retrieving loan reports"));

        verify(getLoanReportUseCase, times(1)).getLoanReport();
    }

    @Test
    void shouldSendTestReportSuccessfully() {
        // Given
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.empty());

        // When & Then
        webTestClient.post()
                .uri("/api/v1/reportes/send-now")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Daily business report sent successfully");

        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
    }

    @Test
    void shouldHandleSendTestReportError() {
        // Given
        RuntimeException expectedError = new RuntimeException("Email service unavailable");
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.error(expectedError));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/reportes/send-now")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(body -> body.contains("Error sending report"));

        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
    }

    @Test
    void shouldGetEmptyLoanReport() {
        // Given
        LoanReport emptyReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(0L)
                .totalApprovedAmount(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(getLoanReportUseCase.getLoanReport()).thenReturn(Mono.just(emptyReport));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/reportes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoanReport.class)
                .isEqualTo(emptyReport);

        verify(getLoanReportUseCase, times(1)).getLoanReport();
    }

    @Test
    void shouldGetLoanReportWithLargeNumbers() {
        // Given
        LoanReport largeReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(999L)
                .totalApprovedAmount(new BigDecimal("99999999.99"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(getLoanReportUseCase.getLoanReport()).thenReturn(Mono.just(largeReport));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/reportes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoanReport.class)
                .isEqualTo(largeReport);

        verify(getLoanReportUseCase, times(1)).getLoanReport();
    }
}

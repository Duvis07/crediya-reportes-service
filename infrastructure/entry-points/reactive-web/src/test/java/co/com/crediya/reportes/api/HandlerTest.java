package co.com.crediya.reportes.api;

import co.com.crediya.reportes.model.LoanReport;
import co.com.crediya.reportes.usecase.GetLoanReportUseCase;
import co.com.crediya.reportes.usecase.SendDailyReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandlerTest {

    @Mock
    private GetLoanReportUseCase getLoanReportUseCase;

    @Mock
    private SendDailyReportUseCase sendDailyReportUseCase;

    @Mock
    private ServerRequest serverRequest;

    private Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Handler(getLoanReportUseCase, sendDailyReportUseCase);
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

        // When
        Mono<ServerResponse> response = handler.getLoanReports(serverRequest);

        // Then
        StepVerifier.create(response)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                })
                .verifyComplete();

        verify(getLoanReportUseCase, times(1)).getLoanReport();
    }

    @Test
    void shouldHandleGetLoanReportsError() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database connection failed");
        when(getLoanReportUseCase.getLoanReport()).thenReturn(Mono.error(expectedError));

        // When
        Mono<ServerResponse> response = handler.getLoanReports(serverRequest);

        // Then
        StepVerifier.create(response)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, serverResponse.statusCode());
                })
                .verifyComplete();

        verify(getLoanReportUseCase, times(1)).getLoanReport();
    }

    @Test
    void shouldSendTestReportSuccessfully() {
        // Given
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.empty());

        // When
        Mono<ServerResponse> response = handler.sendTestReport(serverRequest);

        // Then
        StepVerifier.create(response)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                })
                .verifyComplete();

        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
    }

    @Test
    void shouldHandleSendTestReportError() {
        // Given
        RuntimeException expectedError = new RuntimeException("Email service unavailable");
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.error(expectedError));

        // When
        Mono<ServerResponse> response = handler.sendTestReport(serverRequest);

        // Then
        StepVerifier.create(response)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, serverResponse.statusCode());
                })
                .verifyComplete();

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

        // When
        Mono<ServerResponse> response = handler.getLoanReports(serverRequest);

        // Then
        StepVerifier.create(response)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                })
                .verifyComplete();

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

        // When
        Mono<ServerResponse> response = handler.getLoanReports(serverRequest);

        // Then
        StepVerifier.create(response)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                })
                .verifyComplete();

        verify(getLoanReportUseCase, times(1)).getLoanReport();
    }


    @Test
    void shouldVerifyHandlerConstructor() {
        // When
        Handler newHandler = new Handler(getLoanReportUseCase, sendDailyReportUseCase);

        // Then
        assertNotNull(newHandler);
    }
}

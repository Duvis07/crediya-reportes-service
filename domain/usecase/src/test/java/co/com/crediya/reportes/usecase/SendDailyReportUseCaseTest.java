package co.com.crediya.reportes.usecase;

import co.com.crediya.reportes.model.LoanReport;
import co.com.crediya.reportes.model.gateways.LoanReportRepository;
import co.com.crediya.reportes.model.gateways.ReportEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendDailyReportUseCaseTest {

    @Mock
    private LoanReportRepository loanReportRepository;

    @Mock
    private ReportEmailService reportEmailService;

    private SendDailyReportUseCase sendDailyReportUseCase;

    @BeforeEach
    void setUp() {
        sendDailyReportUseCase = new SendDailyReportUseCase(loanReportRepository, reportEmailService);
    }

    @Test
    void shouldSendDailyBusinessReportSuccessfully() {
        // Given
        LoanReport loanReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(10L)
                .totalApprovedAmount(new BigDecimal("5000000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(loanReportRepository.getOrCreateSummary()).thenReturn(Mono.just(loanReport));
        when(reportEmailService.sendDailyBusinessReport(loanReport)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = sendDailyReportUseCase.sendDailyBusinessReport();

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldHandleRepositoryError() {
        // Given
        RuntimeException error = new RuntimeException("Database connection failed");
        when(loanReportRepository.getOrCreateSummary()).thenReturn(Mono.error(error));

        // When
        Mono<Void> result = sendDailyReportUseCase.sendDailyBusinessReport();

        // Then
        StepVerifier.create(result)
                .verifyComplete(); // Should complete due to error handling
    }

    @Test
    void shouldHandleEmailServiceError() {
        // Given
        LoanReport loanReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(5L)
                .totalApprovedAmount(new BigDecimal("2500000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        RuntimeException emailError = new RuntimeException("Email service unavailable");
        when(loanReportRepository.getOrCreateSummary()).thenReturn(Mono.just(loanReport));
        when(reportEmailService.sendDailyBusinessReport(loanReport)).thenReturn(Mono.error(emailError));

        // When
        Mono<Void> result = sendDailyReportUseCase.sendDailyBusinessReport();

        // Then
        StepVerifier.create(result)
                .verifyComplete(); // Should complete due to error handling
    }

    @Test
    void shouldSendReportWithEmptyData() {
        // Given
        LoanReport emptyReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(0L)
                .totalApprovedAmount(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(loanReportRepository.getOrCreateSummary()).thenReturn(Mono.just(emptyReport));
        when(reportEmailService.sendDailyBusinessReport(emptyReport)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = sendDailyReportUseCase.sendDailyBusinessReport();

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
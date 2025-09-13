package co.com.crediya.reportes.usecase;

import co.com.crediya.reportes.model.LoanReport;
import co.com.crediya.reportes.model.gateways.LoanReportRepository;
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
class GetLoanReportUseCaseTest {

    @Mock
    private LoanReportRepository loanReportRepository;

    private GetLoanReportUseCase getLoanReportUseCase;

    @BeforeEach
    void setUp() {
        getLoanReportUseCase = new GetLoanReportUseCase(loanReportRepository);
    }

    @Test
    void shouldGetLoanReportSuccessfully() {
        // Given
        LoanReport expectedReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(5L)
                .totalApprovedAmount(new BigDecimal("2500000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(loanReportRepository.getOrCreateSummary()).thenReturn(Mono.just(expectedReport));

        // When
        Mono<LoanReport> result = getLoanReportUseCase.getLoanReport();

        // Then
        StepVerifier.create(result)
                .expectNext(expectedReport)
                .verifyComplete();
    }

    @Test
    void shouldHandleRepositoryError() {
        // Given
        RuntimeException error = new RuntimeException("Database connection failed");
        when(loanReportRepository.getOrCreateSummary()).thenReturn(Mono.error(error));

        // When
        Mono<LoanReport> result = getLoanReportUseCase.getLoanReport();

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldGetEmptyReport() {
        // Given
        LoanReport emptyReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(0L)
                .totalApprovedAmount(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(loanReportRepository.getOrCreateSummary()).thenReturn(Mono.just(emptyReport));

        // When
        Mono<LoanReport> result = getLoanReportUseCase.getLoanReport();

        // Then
        StepVerifier.create(result)
                .assertNext(report -> {
                    assert report.getTotalApprovedLoans().equals(0L);
                    assert report.getTotalApprovedAmount().equals(BigDecimal.ZERO);
                })
                .verifyComplete();
    }
}
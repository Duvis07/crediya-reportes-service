package co.com.crediya.reportes.usecase;

import co.com.crediya.reportes.model.LoanApprovedEvent;
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
class ProcessLoanApprovedEventUseCaseTest {

    @Mock
    private LoanReportRepository loanReportRepository;

    private ProcessLoanApprovedEventUseCase processLoanApprovedEventUseCase;

    private static final String REPORT_ID = "LOAN_REPORT_SUMMARY";

    @BeforeEach
    void setUp() {
        processLoanApprovedEventUseCase = new ProcessLoanApprovedEventUseCase(loanReportRepository);
    }

    @Test
    void shouldProcessLoanApprovedEventSuccessfully() {
        // Given
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .solicitudId("SOL-001")
                .clientEmail("test@example.com")
                .approvedAmount(new BigDecimal("500000"))
                .approvedDate(LocalDateTime.now())
                .eventType("LOAN_APPROVED")
                .build();

        LoanReport updatedReport = LoanReport.builder()
                .id(REPORT_ID)
                .totalApprovedLoans(1L)
                .totalApprovedAmount(new BigDecimal("500000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(loanReportRepository.incrementApprovedLoan(REPORT_ID, new BigDecimal("500000")))
                .thenReturn(Mono.just(updatedReport));

        // When
        Mono<Void> result = processLoanApprovedEventUseCase.processLoanApprovedEvent(event);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldHandleRepositoryError() {
        // Given
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .solicitudId("SOL-002")
                .clientEmail("test2@example.com")
                .approvedAmount(new BigDecimal("750000"))
                .approvedDate(LocalDateTime.now())
                .eventType("LOAN_APPROVED")
                .build();

        RuntimeException error = new RuntimeException("Database error");
        when(loanReportRepository.incrementApprovedLoan(REPORT_ID, new BigDecimal("750000")))
                .thenReturn(Mono.error(error));

        // When
        Mono<Void> result = processLoanApprovedEventUseCase.processLoanApprovedEvent(event);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldProcessEventWithZeroAmount() {
        // Given
        LoanApprovedEvent event = LoanApprovedEvent.builder()
                .solicitudId("SOL-003")
                .clientEmail("test3@example.com")
                .approvedAmount(BigDecimal.ZERO)
                .approvedDate(LocalDateTime.now())
                .eventType("LOAN_APPROVED")
                .build();

        LoanReport updatedReport = LoanReport.builder()
                .id(REPORT_ID)
                .totalApprovedLoans(1L)
                .totalApprovedAmount(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(loanReportRepository.incrementApprovedLoan(REPORT_ID, BigDecimal.ZERO))
                .thenReturn(Mono.just(updatedReport));

        // When
        Mono<Void> result = processLoanApprovedEventUseCase.processLoanApprovedEvent(event);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
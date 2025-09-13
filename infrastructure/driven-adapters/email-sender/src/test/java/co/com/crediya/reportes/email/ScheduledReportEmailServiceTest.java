package co.com.crediya.reportes.email;

import co.com.crediya.reportes.model.LoanReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledReportEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private ScheduledReportEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new ScheduledReportEmailService(mailSender) {
            @Override
            protected Mono<String> loadEmailTemplate() {
                return Mono.just("<html><body>Report Date: {{reportDate}}<br>Total Loans: {{totalLoans}}<br>Total Amount: {{totalAmount}}<br>Last Updated: {{lastUpdated}}</body></html>");
            }
            
            @Override
            protected Mono<Void> sendEmail(String to, String subject, String htmlContent) {
                return Mono.empty();
            }
        };
        
        List<String> adminEmails = Arrays.asList("admin1@crediya.com", "admin2@crediya.com");
        ReflectionTestUtils.setField(emailService, "adminEmails", adminEmails);
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

        // When
        Mono<Void> result = emailService.sendDailyBusinessReport(loanReport);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldHandleEmptyLoanReport() {
        // Given
        LoanReport emptyReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(0L)
                .totalApprovedAmount(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();

        // When
        Mono<Void> result = emailService.sendDailyBusinessReport(emptyReport);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldHandleTemplateLoadingError() {
        // Given
        ScheduledReportEmailService errorService = new ScheduledReportEmailService(mailSender) {
            @Override
            protected Mono<String> loadEmailTemplate() {
                return Mono.error(new RuntimeException("Template not found"));
            }
        };
        
        List<String> adminEmails = Arrays.asList("admin@crediya.com");
        ReflectionTestUtils.setField(errorService, "adminEmails", adminEmails);

        LoanReport loanReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(5L)
                .totalApprovedAmount(new BigDecimal("2500000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        // When
        Mono<Void> result = errorService.sendDailyBusinessReport(loanReport);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldSendToMultipleAdmins() {
        // Given
        List<String> multipleAdmins = Arrays.asList("admin1@crediya.com", "admin2@crediya.com", "admin3@crediya.com");
        ReflectionTestUtils.setField(emailService, "adminEmails", multipleAdmins);

        LoanReport loanReport = LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(15L)
                .totalApprovedAmount(new BigDecimal("7500000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        // When
        Mono<Void> result = emailService.sendDailyBusinessReport(loanReport);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
}

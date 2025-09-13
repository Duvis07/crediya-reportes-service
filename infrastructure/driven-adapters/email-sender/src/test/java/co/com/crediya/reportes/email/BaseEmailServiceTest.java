package co.com.crediya.reportes.email;

import co.com.crediya.reportes.model.exceptions.EmailNotificationException;
import co.com.crediya.reportes.model.exceptions.EmailTemplateException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private TestBaseEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new TestBaseEmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@crediya.com");
    }

    @Test
    void shouldLoadEmailTemplateSuccessfully() {
        // When
        Mono<String> result = emailService.loadEmailTemplate();

        // Then
        StepVerifier.create(result)
                .expectNextMatches(template -> template.contains("{{reportDate}}"))
                .verifyComplete();
    }

    @Test
    void shouldSendEmailSuccessfully() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        Mono<Void> result = emailService.sendEmail("test@example.com", "Test Subject", "<html>Test Content</html>");

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldHandleEmailSendingError() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // When
        Mono<Void> result = emailService.sendEmail("test@example.com", "Test Subject", "<html>Test Content</html>");

        // Then
        StepVerifier.create(result)
                .expectError(EmailNotificationException.class)
                .verify();
    }

    @Test
    void shouldHandleTemplateNotFound() {
        // Given
        TestBaseEmailService errorService = new TestBaseEmailService(mailSender) {
            @Override
            protected Mono<String> loadEmailTemplate() {
                return Mono.error(new EmailTemplateException("Template not found", new RuntimeException()));
            }
        };

        // When
        Mono<String> result = errorService.loadEmailTemplate();

        // Then
        StepVerifier.create(result)
                .expectError(EmailTemplateException.class)
                .verify();
    }

    // Test implementation of BaseEmailService
    private static class TestBaseEmailService extends BaseEmailService {
        public TestBaseEmailService(JavaMailSender mailSender) {
            super(mailSender);
        }

        @Override
        protected Mono<String> loadEmailTemplate() {
            return Mono.just("<html><body>Report Date: {{reportDate}}<br>Total Loans: {{totalLoans}}</body></html>");
        }
    }
}

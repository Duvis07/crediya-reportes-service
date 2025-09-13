package co.com.crediya.reportes.email;

import co.com.crediya.reportes.model.exceptions.EmailNotificationException;
import co.com.crediya.reportes.model.exceptions.EmailTemplateException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseEmailService {

    protected final JavaMailSender mailSender;

    @Value("${spring.mail.from:crediya@localhost}")
    protected String fromEmail;

    protected static final String UTF8_ENCODING = "UTF-8";
    protected static final String PLACEHOLDER_CLEANUP = "\\{\\{[^}]*}}";

    /**
     * Loads email template from classpath resources (reactive version)
     */
    protected Mono<String> loadEmailTemplate() {
        return Mono.fromCallable(() -> {
            try {
                ClassPathResource resource = new ClassPathResource("email-templates/" + "daily-business-report.html");
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Failed to load email template: {}", "daily-business-report.html", e);
                throw new EmailTemplateException("Failed to load email template: " + "daily-business-report.html", e);
            }
        });
    }

    /**
     * Sends email using JavaMailSender (reactive version)
     */
    protected Mono<Void> sendEmail(String to, String subject, String htmlContent) {
        return Mono.fromCallable(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);
                
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                helper.setFrom(fromEmail);
                
                mailSender.send(message);
                log.info("Email sent successfully to: {}", to);
                return null;
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", to, e.getMessage());
                throw new EmailNotificationException("Failed to send email to: " + to, e);
            }
        });
    }
}

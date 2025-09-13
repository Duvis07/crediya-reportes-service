package co.com.crediya.reportes.email;

import co.com.crediya.reportes.model.LoanReport;
import co.com.crediya.reportes.model.gateways.ReportEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class ScheduledReportEmailService extends BaseEmailService implements ReportEmailService {

    @Value("${app.admin.emails}")
    private List<String> adminEmails;

    public ScheduledReportEmailService(JavaMailSender mailSender) {
        super(mailSender);
    }

    public Mono<Void> sendDailyBusinessReport(LoanReport loanReport) {
        log.info("Sending daily business report to {} administrators", adminEmails.size());

        return loadEmailTemplate()
                .map(template -> buildReportContent(template, loanReport))
                .flatMap(this::sendToAllAdmins)
                .doOnSuccess(v -> log.info("Daily business report sent successfully to all administrators"))
                .doOnError(error -> log.error("Error sending daily business report", error));
    }

    private String buildReportContent(String template, LoanReport loanReport) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        return template
                .replace("{{reportDate}}", now.format(dateFormatter))
                .replace("{{totalLoans}}", String.valueOf(loanReport.getTotalApprovedLoans()))
                .replace("{{totalAmount}}", currencyFormatter.format(loanReport.getTotalApprovedAmount()))
                .replace("{{lastUpdated}}", loanReport.getLastUpdated().format(dateFormatter))
                .replaceAll(PLACEHOLDER_CLEANUP, "");
    }

    private Mono<Void> sendToAllAdmins(String htmlContent) {
        String subject = "📊 Reporte Diario de Negocio - CrediYa";

        return Mono.when(
                adminEmails.stream()
                        .map(email -> sendEmail(email, subject, htmlContent)
                                .doOnSuccess(v -> log.info("Report sent to admin: {}", email))
                                .onErrorResume(error -> {
                                    log.error("Failed to send report to admin: {}", email, error);
                                    return Mono.empty(); // Continue with other emails
                                }))
                        .toArray(Mono[]::new)
        );
    }
}

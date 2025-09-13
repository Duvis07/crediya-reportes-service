package co.com.crediya.reportes.usecase;

import co.com.crediya.reportes.model.gateways.LoanReportRepository;
import co.com.crediya.reportes.model.gateways.ReportEmailService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class SendDailyReportUseCase {

    private final LoanReportRepository loanReportRepository;
    private final ReportEmailService reportEmailService;
    
    private final Logger log = Logger.getLogger(getClass().getName());

    public Mono<Void> sendDailyBusinessReport() {
        log.info("Starting daily business report generation and sending");

        return loanReportRepository.getOrCreateSummary()
                .flatMap(loanReport -> {
                    log.info("Retrieved loan report data: " + loanReport.getTotalApprovedLoans() 
                            + " loans, total amount: $" + loanReport.getTotalApprovedAmount());
                    
                    return reportEmailService.sendDailyBusinessReport(loanReport);
                })
                .doOnSuccess(v -> log.info("Daily business report sent successfully"))
                .doOnError(error -> log.severe("Error sending daily business report: " + error.getMessage()))
                .onErrorResume(error -> {
                    log.severe("Failed to send daily business report, operation completed with error handling");
                    return Mono.empty(); // Graceful degradation
                });
    }
}

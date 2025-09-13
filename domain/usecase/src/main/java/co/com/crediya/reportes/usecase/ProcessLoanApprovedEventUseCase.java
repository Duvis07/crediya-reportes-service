package co.com.crediya.reportes.usecase;

import co.com.crediya.reportes.model.LoanApprovedEvent;
import co.com.crediya.reportes.model.gateways.LoanReportRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;


@RequiredArgsConstructor
public class ProcessLoanApprovedEventUseCase {

    private final LoanReportRepository loanReportRepository;
    private static final String REPORT_ID = "LOAN_REPORT_SUMMARY";
    private final Logger log = Logger.getLogger(getClass().getName());

    public Mono<Void> processLoanApprovedEvent(LoanApprovedEvent event) {
        log.info("Processing loan approved event for solicitud: {}, approved amount: {}"
        );

        return loanReportRepository.incrementApprovedLoan(REPORT_ID, event.getApprovedAmount())
                .doOnSuccess(updatedReport -> log.info("Updated loan report: {} total loans, {} total amount"
                ))
                .doOnError(error -> log.severe("Error processing loan approved event for solicitud: {}" +
                        event.getSolicitudId()))
                .then();
    }
}

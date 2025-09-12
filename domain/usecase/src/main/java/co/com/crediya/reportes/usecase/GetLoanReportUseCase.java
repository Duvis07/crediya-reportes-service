package co.com.crediya.reportes.usecase;

import co.com.crediya.reportes.model.LoanReport;
import co.com.crediya.reportes.model.gateways.LoanReportRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;


@RequiredArgsConstructor
public class GetLoanReportUseCase {

    private final LoanReportRepository loanReportRepository;

    Logger log = Logger.getLogger(getClass().getName());

    public Mono<LoanReport> getLoanReport() {
        log.info("Getting loan report summary");

        return loanReportRepository.getOrCreateSummary()
                .doOnSuccess(report -> log.info("Retrieved loan report: {} approved loans, total amount: {}"
                ))
                .doOnError(error -> log.severe("Error retrieving loan report: " + error.getMessage()));
    }
}

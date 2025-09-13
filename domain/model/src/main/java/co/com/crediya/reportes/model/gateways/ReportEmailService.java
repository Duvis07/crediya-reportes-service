package co.com.crediya.reportes.model.gateways;

import co.com.crediya.reportes.model.LoanReport;
import reactor.core.publisher.Mono;

public interface ReportEmailService {
    
    Mono<Void> sendDailyBusinessReport(LoanReport loanReport);
}

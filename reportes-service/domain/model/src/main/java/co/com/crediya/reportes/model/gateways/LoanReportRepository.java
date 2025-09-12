package co.com.crediya.reportes.model.gateways;

import co.com.crediya.reportes.model.LoanReport;
import reactor.core.publisher.Mono;

public interface LoanReportRepository {
    
    Mono<LoanReport> findById(String id);
    
    Mono<LoanReport> save(LoanReport loanReport);
    
    Mono<LoanReport> updateLoanCount(String id, Long increment);
    
    Mono<LoanReport> incrementApprovedLoan(String id, java.math.BigDecimal amount);
    
    Mono<LoanReport> getOrCreateSummary();
}

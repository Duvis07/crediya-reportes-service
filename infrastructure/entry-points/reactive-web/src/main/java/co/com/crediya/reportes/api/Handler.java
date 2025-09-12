package co.com.crediya.reportes.api;

import co.com.crediya.reportes.usecase.GetLoanReportUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    
    private final GetLoanReportUseCase getLoanReportUseCase;

    public Mono<ServerResponse> getLoanReports(ServerRequest serverRequest) {
        log.info("GET /api/v1/reportes - Getting loan reports");
        
        return getLoanReportUseCase.getLoanReport()
                .flatMap(loanReport -> ServerResponse.ok()
                        .bodyValue(loanReport))
                .doOnSuccess(response -> log.info("Successfully returned loan reports"))
                .doOnError(error -> log.error("Error getting loan reports", error))
                .onErrorResume(error -> ServerResponse.status(500)
                        .bodyValue("Error retrieving loan reports: " + error.getMessage()));
    }
}

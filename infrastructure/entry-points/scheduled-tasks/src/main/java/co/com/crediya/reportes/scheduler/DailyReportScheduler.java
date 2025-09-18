package co.com.crediya.reportes.scheduler;

import co.com.crediya.reportes.usecase.SendDailyReportUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final SendDailyReportUseCase sendDailyReportUseCase;

    @Scheduled(cron = "${DAILY_REPORT_CRON:0 */3 * * * *}")
    public void sendDailyBusinessReport() {
        log.info("Starting scheduled daily business report generation at 8:00 PM");
        
        generateDailyReportReactively()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Scheduled daily business report completed successfully"))
                .doOnError(error -> log.error("Error in scheduled daily business report", error))
                .onErrorResume(error -> {
                    log.error("Daily business report failed, will retry on next schedule", error);
                    return Mono.empty();
                })
                .subscribe();
    }

    private Mono<Void> generateDailyReportReactively() {
        return sendDailyReportUseCase.sendDailyBusinessReport()
                .doOnSubscribe(subscription -> log.debug("Starting daily report generation process"))
                .doOnSuccess(result -> log.debug("Daily report generation process completed"))
                .onErrorResume(error -> {
                    log.error("Failed to generate daily report", error);
                    return Mono.empty();
                });
    }
}

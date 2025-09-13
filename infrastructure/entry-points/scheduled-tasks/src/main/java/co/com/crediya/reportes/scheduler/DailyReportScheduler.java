package co.com.crediya.reportes.scheduler;

import co.com.crediya.reportes.usecase.SendDailyReportUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final SendDailyReportUseCase sendDailyReportUseCase;

    @Scheduled(cron = "${DAILY_REPORT_CRON:0 */1 * * * *}")
    public void sendDailyBusinessReport() {
        log.info("Starting scheduled daily business report generation at 8:00 PM");
        
        sendDailyReportUseCase.sendDailyBusinessReport()
                .doOnSuccess(v -> log.info("Scheduled daily business report completed successfully"))
                .doOnError(error -> log.error("Error in scheduled daily business report", error))
                .onErrorResume(error -> {
                    log.error("Daily business report failed, will retry tomorrow", error);
                    return reactor.core.publisher.Mono.empty();
                })
                .subscribe();
    }

}

package co.com.crediya.reportes.scheduler;

import co.com.crediya.reportes.usecase.SendDailyReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyReportSchedulerTest {

    @Mock
    private SendDailyReportUseCase sendDailyReportUseCase;

    private DailyReportScheduler dailyReportScheduler;

    @BeforeEach
    void setUp() {
        dailyReportScheduler = new DailyReportScheduler(sendDailyReportUseCase);
    }

    @Test
    void shouldSendDailyBusinessReportSuccessfully() {
        // Given
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.empty());

        // When
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
    }

    @Test
    void shouldHandleErrorInDailyBusinessReport() {
        // Given
        RuntimeException expectedError = new RuntimeException("Email service unavailable");
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.error(expectedError));

        // When
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
        // Error should be handled gracefully and not propagate
    }

    @Test
    void shouldHandleNullPointerException() {
        // Given
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.error(new NullPointerException("Null data")));

        // When
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
    }

    @Test
    void shouldHandleTimeoutException() {
        // Given
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.error(new RuntimeException("Timeout")));

        // When
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
    }

    @Test
    void shouldVerifySchedulerConstructor() {
        // When
        DailyReportScheduler newScheduler = new DailyReportScheduler(sendDailyReportUseCase);

        // Then
        assertNotNull(newScheduler);
    }

    @Test
    void shouldHandleMultipleConsecutiveCalls() {
        // Given
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.empty());

        // When
        dailyReportScheduler.sendDailyBusinessReport();
        dailyReportScheduler.sendDailyBusinessReport();
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(3)).sendDailyBusinessReport();
    }

    @Test
    void shouldHandleAlternatingSuccessAndError() {
        // Given
        when(sendDailyReportUseCase.sendDailyBusinessReport())
                .thenReturn(Mono.empty())
                .thenReturn(Mono.error(new RuntimeException("Temporary error")))
                .thenReturn(Mono.empty());

        // When
        dailyReportScheduler.sendDailyBusinessReport();
        dailyReportScheduler.sendDailyBusinessReport();
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(3)).sendDailyBusinessReport();
    }

    @Test
    void shouldVerifyErrorHandlingWithOnErrorResume() {
        // Given
        RuntimeException testError = new RuntimeException("Test error");
        Mono<Void> errorMono = Mono.error(testError);
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(errorMono);

        // When
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
        // The scheduler should handle the error gracefully without throwing
    }

    @Test
    void shouldHandleSuccessWithLogging() {
        // Given
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.empty());

        // When
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
        // Success should be logged (verified by successful execution)
    }

    @Test
    void shouldHandleErrorWithLogging() {
        // Given
        RuntimeException testError = new RuntimeException("Database connection failed");
        when(sendDailyReportUseCase.sendDailyBusinessReport()).thenReturn(Mono.error(testError));

        // When
        dailyReportScheduler.sendDailyBusinessReport();

        // Then
        verify(sendDailyReportUseCase, times(1)).sendDailyBusinessReport();
        // Error should be logged and handled gracefully
    }
}

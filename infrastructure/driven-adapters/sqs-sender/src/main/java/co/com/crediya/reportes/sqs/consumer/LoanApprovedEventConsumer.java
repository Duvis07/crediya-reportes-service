package co.com.crediya.reportes.sqs.consumer;

import co.com.crediya.reportes.model.LoanApprovedEvent;
import co.com.crediya.reportes.model.exceptions.EventProcessingException;
import co.com.crediya.reportes.usecase.ProcessLoanApprovedEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanApprovedEventConsumer {

    private final ProcessLoanApprovedEventUseCase processLoanApprovedEventUseCase;
    private final ObjectMapper objectMapper;

    @SqsListener(value = "${sqs.queues.loan-approved-events}")
    public void consumeLoanApprovedEvent(String message) {
        log.info("Received loan approved event message: {}", message);
        
        try {
            LoanApprovedEvent event = objectMapper.readValue(message, LoanApprovedEvent.class);
            
            processLoanApprovedEventUseCase.processLoanApprovedEvent(event)
                    .doOnSuccess(result -> log.info("Successfully processed loan approved event for solicitud: {}", 
                            event.getSolicitudId()))
                    .doOnError(error -> log.error("Error processing loan approved event for solicitud: {}", 
                            event.getSolicitudId(), error))
                    .onErrorResume(error -> {
                        log.error("Failed to process loan approved event, will be retried by SQS", error);
                        return Mono.error(error); // Let SQS handle retry
                    })
                    .block(); // Block for SQS listener compatibility
                    
        } catch (Exception e) {
            log.error("Error parsing loan approved event message: {}", message, e);
            throw new EventProcessingException("Failed to parse loan approved event", e);
        }
    }
}

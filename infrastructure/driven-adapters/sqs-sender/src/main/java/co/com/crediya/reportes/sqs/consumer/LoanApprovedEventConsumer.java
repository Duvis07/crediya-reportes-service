package co.com.crediya.reportes.sqs.consumer;

import co.com.crediya.reportes.model.LoanApprovedEvent;
import co.com.crediya.reportes.usecase.ProcessLoanApprovedEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanApprovedEventConsumer {

    private final ProcessLoanApprovedEventUseCase processLoanApprovedEventUseCase;
    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;

    @Value("${AWS_SQS_QUEUES_LOAN_APPROVED_EVENTS:loan-approved-events-queue}")
    private String queueUrl;

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    public void consumeLoanApprovedEvent() {
        try {
            log.debug("Polling SQS queue: {}", queueUrl);
            
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
            
            if (!messages.isEmpty()) {
                log.info("Found {} messages in queue: {}", messages.size(), queueUrl);
                for (Message message : messages) {
                    processMessage(message, queueUrl);
                }
            } else {
                log.debug("No messages found in queue: {}", queueUrl);
            }
        } catch (Exception e) {
            log.error("Error polling SQS queue: {}", queueUrl, e);
        }
    }

    private void processMessage(Message message, String queueUrl) {
        try {
            log.info("Received loan approved event message: {}", message.body());
            
            LoanApprovedEvent event = objectMapper.readValue(message.body(), LoanApprovedEvent.class);
            
            processLoanApprovedEventUseCase.processLoanApprovedEvent(event)
                    .doOnSuccess(result -> {
                        log.info("Successfully processed loan approved event for solicitud: {}", event.getSolicitudId());
                        deleteMessage(message, queueUrl);
                    })
                    .doOnError(error -> log.error("Error processing loan approved event for solicitud: {}", 
                            event.getSolicitudId(), error))
                    .onErrorResume(error -> {
                        log.error("Failed to process loan approved event, message will remain in queue for retry", error);
                        return Mono.empty();
                    })
                    .block();
                    
        } catch (Exception e) {
            log.error("Error parsing loan approved event message: {}", message.body(), e);
        }
    }

    private void deleteMessage(Message message, String queueUrl) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteRequest);
            log.debug("Message deleted from queue: {}", message.messageId());
        } catch (Exception e) {
            log.error("Error deleting message from queue: {}", message.messageId(), e);
        }
    }
}

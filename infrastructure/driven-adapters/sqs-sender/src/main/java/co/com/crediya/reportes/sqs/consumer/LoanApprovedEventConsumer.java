package co.com.crediya.reportes.sqs.consumer;

import co.com.crediya.reportes.model.LoanApprovedEvent;
import co.com.crediya.reportes.model.exceptions.SqsMessageDeletionException;
import co.com.crediya.reportes.usecase.ProcessLoanApprovedEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
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
        pollMessagesReactively()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.debug("Polling completed successfully"))
                .doOnError(error -> log.error("Error during reactive polling", error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    private Mono<Void> pollMessagesReactively() {
        return Mono.fromCallable(() -> {
                    log.debug("Polling SQS queue: {}", queueUrl);

                    ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .maxNumberOfMessages(10)
                            .waitTimeSeconds(5)
                            .build();

                    return sqsClient.receiveMessage(receiveRequest).messages();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::processMessagesReactively)
                .onErrorResume(error -> {
                    log.error("Error polling SQS queue: {}", queueUrl, error);
                    return Mono.empty();
                });
    }

    private Mono<Void> processMessagesReactively(List<Message> messages) {
        if (messages.isEmpty()) {
            log.debug("No messages found in queue: {}", queueUrl);
            return Mono.empty();
        }

        log.info("Found {} messages in queue: {}", messages.size(), queueUrl);

        return Flux.fromIterable(messages)
                .flatMap(this::processMessageReactively)
                .then();
    }

    private Mono<Void> processMessageReactively(Message message) {
        return Mono.fromCallable(() -> {
                    log.info("Received loan approved event message: {}", message.body());
                    return objectMapper.readValue(message.body(), LoanApprovedEvent.class);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(event -> processLoanApprovedEventUseCase.processLoanApprovedEvent(event)
                        .doOnSuccess(result -> log.info("Successfully processed loan approved event for solicitud: {}", event.getSolicitudId()))
                        .then(deleteMessageReactively(message))
                )
                .onErrorResume(error -> {
                    log.error("Error processing loan approved event message: {}", message.body(), error);
                    return Mono.empty();
                });
    }

    private Mono<Void> deleteMessageReactively(Message message) {
        return Mono.fromRunnable(() -> {
                    try {
                        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .receiptHandle(message.receiptHandle())
                                .build();
                        sqsClient.deleteMessage(deleteRequest);
                        log.debug("Message deleted from queue: {}", message.messageId());
                    } catch (Exception e) {
                        log.error("Error deleting message from queue: {}", message.messageId(), e);
                        throw new SqsMessageDeletionException("Failed to delete message", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}

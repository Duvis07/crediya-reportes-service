package co.com.crediya.reportes.sqs.consumer;

import co.com.crediya.reportes.model.LoanApprovedEvent;
import co.com.crediya.reportes.usecase.ProcessLoanApprovedEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApprovedEventConsumerTest {

    private static final String QUEUE_URL = "test-queue-url";
    private static final String SOLICITUD_ID = "SOL-123";
    private static final BigDecimal APPROVED_AMOUNT = new BigDecimal("10000.00");
    private static final String MESSAGE_ID = "msg-123";
    private static final String RECEIPT_HANDLE = "receipt-123";

    @Mock
    private ProcessLoanApprovedEventUseCase processLoanApprovedEventUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SqsClient sqsClient;

    private LoanApprovedEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new LoanApprovedEventConsumer(
            processLoanApprovedEventUseCase,
            objectMapper,
            sqsClient
        );
        
        // Set queue URL using reflection
        try {
            var field = LoanApprovedEventConsumer.class.getDeclaredField("queueUrl");
            field.setAccessible(true);
            field.set(consumer, QUEUE_URL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldProcessMessagesSuccessfully() throws Exception {
        // Given
        LoanApprovedEvent event = createLoanApprovedEvent();
        Message message = createMessage();
        String messageBody = "{\"solicitudId\":\"SOL-123\",\"approvedAmount\":10000.00}";
        
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(List.of(message))
                .build();
        
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveResponse);
        when(objectMapper.readValue(messageBody, LoanApprovedEvent.class))
                .thenReturn(event);
        when(processLoanApprovedEventUseCase.processLoanApprovedEvent(event))
                .thenReturn(Mono.empty());

        // When
        consumer.consumeLoanApprovedEvent();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(messageBody, LoanApprovedEvent.class);
        verify(processLoanApprovedEventUseCase).processLoanApprovedEvent(event);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldHandleEmptyQueue() {
        // Given
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(Collections.emptyList())
                .build();
        
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveResponse);

        // When
        consumer.consumeLoanApprovedEvent();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(processLoanApprovedEventUseCase);
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldHandleJsonParsingError() throws Exception {
        // Given
        Message message = createMessage();
        String invalidJson = "invalid-json";
        message = message.toBuilder().body(invalidJson).build();
        
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(List.of(message))
                .build();
        
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveResponse);
        when(objectMapper.readValue(invalidJson, LoanApprovedEvent.class))
                .thenThrow(new RuntimeException("JSON parsing error"));

        // When
        consumer.consumeLoanApprovedEvent();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(invalidJson, LoanApprovedEvent.class);
        verifyNoInteractions(processLoanApprovedEventUseCase);
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldHandleProcessingError() throws Exception {
        // Given
        LoanApprovedEvent event = createLoanApprovedEvent();
        Message message = createMessage();
        String messageBody = "{\"solicitudId\":\"SOL-123\",\"approvedAmount\":10000.00}";
        
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(List.of(message))
                .build();
        
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveResponse);
        when(objectMapper.readValue(messageBody, LoanApprovedEvent.class))
                .thenReturn(event);
        when(processLoanApprovedEventUseCase.processLoanApprovedEvent(event))
                .thenReturn(Mono.error(new RuntimeException("Processing error")));

        // When
        consumer.consumeLoanApprovedEvent();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(messageBody, LoanApprovedEvent.class);
        verify(processLoanApprovedEventUseCase).processLoanApprovedEvent(event);
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldHandleSqsReceiveError() {
        // Given
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS error"));

        // When
        consumer.consumeLoanApprovedEvent();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(processLoanApprovedEventUseCase);
    }

    @Test
    void shouldHandleDeleteMessageError() throws Exception {
        // Given
        LoanApprovedEvent event = createLoanApprovedEvent();
        Message message = createMessage();
        String messageBody = "{\"solicitudId\":\"SOL-123\",\"approvedAmount\":10000.00}";
        
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(List.of(message))
                .build();
        
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveResponse);
        when(objectMapper.readValue(messageBody, LoanApprovedEvent.class))
                .thenReturn(event);
        when(processLoanApprovedEventUseCase.processLoanApprovedEvent(event))
                .thenReturn(Mono.empty());
        doThrow(new RuntimeException("Delete error"))
                .when(sqsClient).deleteMessage(any(DeleteMessageRequest.class));

        // When
        consumer.consumeLoanApprovedEvent();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(messageBody, LoanApprovedEvent.class);
        verify(processLoanApprovedEventUseCase).processLoanApprovedEvent(event);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldProcessMultipleMessages() throws Exception {
        // Given
        LoanApprovedEvent event1 = createLoanApprovedEvent();
        LoanApprovedEvent event2 = createLoanApprovedEvent();
        event2.setSolicitudId("SOL-456");
        
        Message message1 = createMessage();
        Message message2 = message1.toBuilder()
                .messageId("msg-456")
                .receiptHandle("receipt-456")
                .body("{\"solicitudId\":\"SOL-456\",\"approvedAmount\":10000.00}")
                .build();
        
        String messageBody1 = "{\"solicitudId\":\"SOL-123\",\"approvedAmount\":10000.00}";
        String messageBody2 = "{\"solicitudId\":\"SOL-456\",\"approvedAmount\":10000.00}";
        
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(List.of(message1, message2))
                .build();
        
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveResponse);
        when(objectMapper.readValue(messageBody1, LoanApprovedEvent.class))
                .thenReturn(event1);
        when(objectMapper.readValue(messageBody2, LoanApprovedEvent.class))
                .thenReturn(event2);
        when(processLoanApprovedEventUseCase.processLoanApprovedEvent(any(LoanApprovedEvent.class)))
                .thenReturn(Mono.empty());

        // When
        consumer.consumeLoanApprovedEvent();

        // Then
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(messageBody1, LoanApprovedEvent.class);
        verify(objectMapper).readValue(messageBody2, LoanApprovedEvent.class);
        verify(processLoanApprovedEventUseCase).processLoanApprovedEvent(event1);
        verify(processLoanApprovedEventUseCase).processLoanApprovedEvent(event2);
        verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
    }

    private LoanApprovedEvent createLoanApprovedEvent() {
        LoanApprovedEvent event = new LoanApprovedEvent();
        event.setSolicitudId(SOLICITUD_ID);
        event.setApprovedAmount(APPROVED_AMOUNT);
        return event;
    }

    private Message createMessage() {
        return Message.builder()
                .messageId(MESSAGE_ID)
                .receiptHandle(RECEIPT_HANDLE)
                .body("{\"solicitudId\":\"SOL-123\",\"approvedAmount\":10000.00}")
                .build();
    }
}

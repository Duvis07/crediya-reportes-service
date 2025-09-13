package co.com.crediya.reportes.dynamodb;

import co.com.crediya.reportes.model.LoanReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanReportRepositoryAdapterTest {

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbClient;

    @Mock
    private DynamoDbAsyncTable<LoanReportEntity> table;

    @Mock
    private ObjectMapper mapper;

    private LoanReportRepositoryAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        when(dynamoDbClient.table(eq("loan-reports"), any(TableSchema.class))).thenReturn(table);
        when(table.createTable()).thenReturn(CompletableFuture.completedFuture(null));

        adapter = new LoanReportRepositoryAdapter(dynamoDbClient, mapper);

        // Use reflection to inject the mocked table
        Field tableField = LoanReportRepositoryAdapter.class.getDeclaredField("table");
        tableField.setAccessible(true);
        tableField.set(adapter, table);
    }

    @Test
    void shouldFindByIdSuccessfully() {
        // Given
        String reportId = "TEST-001";
        LoanReportEntity entity = new LoanReportEntity();
        entity.setId(reportId);
        entity.setTotalApprovedLoans(5L);
        entity.setTotalApprovedAmount(new BigDecimal("2500000"));
        entity.setLastUpdated(LocalDateTime.now());

        LoanReport expectedReport = LoanReport.builder()
                .id(reportId)
                .totalApprovedLoans(5L)
                .totalApprovedAmount(new BigDecimal("2500000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(table.getItem(any(Key.class))).thenReturn(CompletableFuture.completedFuture(entity));
        when(mapper.map(entity, LoanReport.class)).thenReturn(expectedReport);

        // When
        Mono<LoanReport> result = adapter.findById(reportId);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedReport)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        // Given
        String reportId = "NOT-FOUND";
        when(table.getItem(any(Key.class))).thenReturn(CompletableFuture.completedFuture(null));

        // When
        Mono<LoanReport> result = adapter.findById(reportId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldSaveSuccessfully() {
        // Given
        LoanReport report = LoanReport.builder()
                .id("SAVE-001")
                .totalApprovedLoans(3L)
                .totalApprovedAmount(new BigDecimal("1500000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        LoanReportEntity entity = new LoanReportEntity();
        entity.setId("SAVE-001");
        entity.setTotalApprovedLoans(3L);
        entity.setTotalApprovedAmount(new BigDecimal("1500000"));

        when(mapper.map(report, LoanReportEntity.class)).thenReturn(entity);
        when(table.putItem(entity)).thenReturn(CompletableFuture.completedFuture(null));

        // When
        Mono<LoanReport> result = adapter.save(report);

        // Then
        StepVerifier.create(result)
                .expectNext(report)
                .verifyComplete();
    }

    @Test
    void shouldGetOrCreateSummaryWhenExists() {
        // Given
        String summaryId = "LOAN_REPORT_SUMMARY";
        LoanReportEntity existingEntity = new LoanReportEntity();
        existingEntity.setId(summaryId);
        existingEntity.setTotalApprovedLoans(10L);
        existingEntity.setTotalApprovedAmount(new BigDecimal("5000000"));

        LoanReport existingReport = LoanReport.builder()
                .id(summaryId)
                .totalApprovedLoans(10L)
                .totalApprovedAmount(new BigDecimal("5000000"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(table.getItem(any(Key.class))).thenReturn(CompletableFuture.completedFuture(existingEntity));
        when(mapper.map(existingEntity, LoanReport.class)).thenReturn(existingReport);

        // When
        Mono<LoanReport> result = adapter.getOrCreateSummary();

        // Then
        StepVerifier.create(result)
                .expectNext(existingReport)
                .verifyComplete();
    }
}
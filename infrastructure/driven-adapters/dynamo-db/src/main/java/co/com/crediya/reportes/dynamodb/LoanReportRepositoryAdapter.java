package co.com.crediya.reportes.dynamodb;

import co.com.crediya.reportes.model.LoanReport;
import co.com.crediya.reportes.model.gateways.LoanReportRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public class LoanReportRepositoryAdapter implements LoanReportRepository {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoanReportRepositoryAdapter.class);

    private final DynamoDbAsyncTable<LoanReportEntity> table;
    private final ObjectMapper mapper;

    public LoanReportRepositoryAdapter(DynamoDbEnhancedAsyncClient dynamoDbClient, ObjectMapper mapper) {
        this.table = dynamoDbClient.table("loan-reports", TableSchema.fromBean(LoanReportEntity.class));
        this.mapper = mapper;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try {
            table.createTable().join();
            log.info("Table 'loan-reports' created successfully");
        } catch (ResourceInUseException e) {
            log.info("Table 'loan-reports' already exists");
        } catch (Exception e) {
            log.error("Error creating table: {}", e.getMessage());
        }
    }

    @Override
    public Mono<LoanReport> findById(String id) {
        log.info("Finding loan report by id: {}", id);
        
        Key key = Key.builder().partitionValue(id).build();
        
        return Mono.fromFuture(table.getItem(key))
                .map(entity -> mapper.map(entity, LoanReport.class))
                .doOnSuccess(report -> log.info("Found loan report: {}", report))
                .doOnError(error -> log.error("Error finding loan report by id: {}", id, error));
    }

    @Override
    public Mono<LoanReport> save(LoanReport loanReport) {
        log.info("Saving loan report: {}", loanReport);
        
        LoanReportEntity entity = mapper.map(loanReport, LoanReportEntity.class);
        
        return Mono.fromFuture(table.putItem(entity))
                .then(Mono.just(loanReport))
                .doOnSuccess(saved -> log.info("Successfully saved loan report: {}", saved))
                .doOnError(error -> log.error("Error saving loan report", error));
    }

    @Override
    public Mono<LoanReport> updateLoanCount(String id, Long increment) {
        return findById(id)
                .switchIfEmpty(Mono.just(LoanReport.createInitial().toBuilder().id(id).build()))
                .map(report -> report.toBuilder()
                        .totalApprovedLoans(report.getTotalApprovedLoans() + increment)
                        .lastUpdated(LocalDateTime.now())
                        .build())
                .flatMap(this::save);
    }

    @Override
    public Mono<LoanReport> incrementApprovedLoan(String id, BigDecimal amount) {
        return findById(id)
                .switchIfEmpty(Mono.just(LoanReport.createInitial().toBuilder().id(id).build()))
                .map(report -> report.incrementLoan(amount))
                .flatMap(this::save);
    }

    @Override
    public Mono<LoanReport> getOrCreateSummary() {
        String summaryId = "LOAN_REPORT_SUMMARY";
        return findById(summaryId)
                .switchIfEmpty(save(LoanReport.createInitial().toBuilder().id(summaryId).build()));
    }
}

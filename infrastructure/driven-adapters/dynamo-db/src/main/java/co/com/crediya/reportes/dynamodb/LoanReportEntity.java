package co.com.crediya.reportes.dynamodb;

import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/* Enhanced DynamoDB annotations are incompatible with Lombok #1932
         https://github.com/aws/aws-sdk-java-v2/issues/1932*/
@Setter
@DynamoDbBean
public class LoanReportEntity {

    private String id;
    private Long totalApprovedLoans;
    private BigDecimal totalApprovedAmount;
    private LocalDateTime lastUpdated;

    public LoanReportEntity() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    @DynamoDbAttribute("totalApprovedLoans")
    public Long getTotalApprovedLoans() {
        return totalApprovedLoans;
    }

    @DynamoDbAttribute("totalApprovedAmount")
    public BigDecimal getTotalApprovedAmount() {
        return totalApprovedAmount;
    }

    @DynamoDbAttribute("lastUpdated")
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

}

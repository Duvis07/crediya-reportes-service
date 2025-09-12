package co.com.crediya.reportes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LoanReport {
    private String id;
    private Long totalApprovedLoans;
    private BigDecimal totalApprovedAmount;
    private LocalDateTime lastUpdated;
    
    public static LoanReport createInitial() {
        return LoanReport.builder()
                .id("LOAN_REPORT_SUMMARY")
                .totalApprovedLoans(0L)
                .totalApprovedAmount(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    public LoanReport incrementLoan(BigDecimal amount) {
        return LoanReport.builder()
                .id(this.id)
                .totalApprovedLoans(this.totalApprovedLoans + 1)
                .totalApprovedAmount(this.totalApprovedAmount.add(amount))
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}

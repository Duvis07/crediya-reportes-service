package co.com.crediya.reportes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovedEvent {
    private String solicitudId;
    private String clientEmail;
    private BigDecimal approvedAmount;
    private LocalDateTime approvedDate;
    private String eventType;
    
    public static final String EVENT_TYPE_LOAN_APPROVED = "LOAN_APPROVED";
}

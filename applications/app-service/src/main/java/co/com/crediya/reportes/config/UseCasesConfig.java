package co.com.crediya.reportes.config;

import co.com.crediya.reportes.model.gateways.LoanReportRepository;
import co.com.crediya.reportes.model.gateways.ReportEmailService;
import co.com.crediya.reportes.usecase.GetLoanReportUseCase;
import co.com.crediya.reportes.usecase.ProcessLoanApprovedEventUseCase;
import co.com.crediya.reportes.usecase.SendDailyReportUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.crediya.reportes.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {

    @Bean
    public GetLoanReportUseCase getLoanReportUseCase(LoanReportRepository loanReportRepository) {
        return new GetLoanReportUseCase(loanReportRepository);
    }

    @Bean
    public ProcessLoanApprovedEventUseCase processLoanApprovedEventUseCase(LoanReportRepository loanReportRepository) {
        return new ProcessLoanApprovedEventUseCase(loanReportRepository);
    }

    @Bean
    public SendDailyReportUseCase sendDailyReportUseCase(LoanReportRepository loanReportRepository, 
                                                         ReportEmailService reportEmailService) {
        return new SendDailyReportUseCase(loanReportRepository, reportEmailService);
    }
}

package co.com.crediya.reportes.sqs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.time.Duration;

@Configuration
public class SqsConfig {

    private static final Logger log = LoggerFactory.getLogger(SqsConfig.class);

    @Value("${AWS_ENDPOINT_URL:http://localhost:4566}")
    private String endpointUrl;

    @Value("${AWS_ACCESS_KEY_ID:test}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY:test}")
    private String secretKey;

    @Value("${AWS_REGION:us-east-1}")
    private String region;

    @Bean
    public SqsClient sqsClient() {
        
        return SqsClient.builder()
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        log.info("Creating SQS async client with endpoint: {}", endpointUrl);
        
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .httpClient(NettyNioAsyncHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(30))
                        .readTimeout(Duration.ofSeconds(30))
                        .build())
                .build();
    }
}

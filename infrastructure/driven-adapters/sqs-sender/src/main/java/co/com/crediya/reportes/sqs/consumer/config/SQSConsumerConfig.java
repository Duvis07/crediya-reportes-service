package co.com.crediya.reportes.sqs.consumer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SQSConsumerConfig {

    @Bean
    @Profile({"local"})
    public SqsAsyncClient sqsAsyncClient(@Value("${sqs.endpoint}") String endpoint,
                                         @Value("${aws.region}") String region,
                                         @Value("${aws.access-key}") String accessKey,
                                         @Value("${aws.secret-key}") String secretKey) {
        return SqsAsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    @Bean
    @Profile({"dev", "cer", "pdn"})
    public SqsAsyncClient sqsAsyncClientProd(@Value("${aws.region}") String region) {
        return SqsAsyncClient.builder()
                .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .region(Region.of(region))
                .build();
    }
}

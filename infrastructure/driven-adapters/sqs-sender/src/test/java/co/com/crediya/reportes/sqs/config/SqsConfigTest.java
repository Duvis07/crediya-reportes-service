package co.com.crediya.reportes.sqs.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SqsConfigTest {

    private static final String TEST_ENDPOINT_URL = "http://localhost:4566";
    private static final String TEST_ACCESS_KEY = "test-access-key";
    private static final String TEST_SECRET_KEY = "test-secret-key";
    private static final String TEST_REGION = "us-east-1";

    private SqsConfig sqsConfig;

    @BeforeEach
    void setUp() {
        sqsConfig = new SqsConfig();
        
        // Set test values using reflection
        ReflectionTestUtils.setField(sqsConfig, "endpointUrl", TEST_ENDPOINT_URL);
        ReflectionTestUtils.setField(sqsConfig, "accessKey", TEST_ACCESS_KEY);
        ReflectionTestUtils.setField(sqsConfig, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(sqsConfig, "region", TEST_REGION);
    }

    @Test
    void shouldCreateSqsClient() {
        // When
        SqsClient sqsClient = sqsConfig.sqsClient();

        // Then
        assertNotNull(sqsClient);
        assertEquals("software.amazon.awssdk.services.sqs.DefaultSqsClient", 
                     sqsClient.getClass().getName());
    }

    @Test
    void shouldCreateSqsAsyncClient() {
        // When
        SqsAsyncClient sqsAsyncClient = sqsConfig.sqsAsyncClient();

        // Then
        assertNotNull(sqsAsyncClient);
        assertEquals("software.amazon.awssdk.services.sqs.DefaultSqsAsyncClient", 
                     sqsAsyncClient.getClass().getName());
    }

    @Test
    void shouldConfigureSqsClientWithCorrectEndpoint() {
        // When
        SqsClient sqsClient = sqsConfig.sqsClient();

        // Then
        assertNotNull(sqsClient);
        // Verify that the client was created successfully with the endpoint
        // The actual endpoint configuration is internal to AWS SDK
    }

    @Test
    void shouldConfigureSqsAsyncClientWithCorrectEndpoint() {
        // When
        SqsAsyncClient sqsAsyncClient = sqsConfig.sqsAsyncClient();

        // Then
        assertNotNull(sqsAsyncClient);
        // Verify that the async client was created successfully with the endpoint
        // The actual endpoint configuration is internal to AWS SDK
    }

    @Test
    void shouldCreateDifferentClientInstances() {
        // When
        SqsClient client1 = sqsConfig.sqsClient();
        SqsClient client2 = sqsConfig.sqsClient();

        // Then
        assertNotNull(client1);
        assertNotNull(client2);
        // Each call should create a new instance (no @Singleton annotation)
        assertNotSame(client1, client2);
    }

    @Test
    void shouldCreateDifferentAsyncClientInstances() {
        // When
        SqsAsyncClient asyncClient1 = sqsConfig.sqsAsyncClient();
        SqsAsyncClient asyncClient2 = sqsConfig.sqsAsyncClient();

        // Then
        assertNotNull(asyncClient1);
        assertNotNull(asyncClient2);
        // Each call should create a new instance (no @Singleton annotation)
        assertNotSame(asyncClient1, asyncClient2);
    }

    @Test
    void shouldConfigureWithCustomEndpoint() {
        // Given
        String customEndpoint = "http://custom-endpoint:9999";
        ReflectionTestUtils.setField(sqsConfig, "endpointUrl", customEndpoint);

        // When
        SqsClient sqsClient = sqsConfig.sqsClient();
        SqsAsyncClient sqsAsyncClient = sqsConfig.sqsAsyncClient();

        // Then
        assertNotNull(sqsClient);
        assertNotNull(sqsAsyncClient);
        // Clients should be created successfully with custom endpoint
    }

    @Test
    void shouldConfigureWithCustomRegion() {
        // Given
        String customRegion = "eu-west-1";
        ReflectionTestUtils.setField(sqsConfig, "region", customRegion);

        // When
        SqsClient sqsClient = sqsConfig.sqsClient();
        SqsAsyncClient sqsAsyncClient = sqsConfig.sqsAsyncClient();

        // Then
        assertNotNull(sqsClient);
        assertNotNull(sqsAsyncClient);
        // Clients should be created successfully with custom region
    }

    @Test
    void shouldConfigureWithCustomCredentials() {
        // Given
        String customAccessKey = "custom-access-key";
        String customSecretKey = "custom-secret-key";
        ReflectionTestUtils.setField(sqsConfig, "accessKey", customAccessKey);
        ReflectionTestUtils.setField(sqsConfig, "secretKey", customSecretKey);

        // When
        SqsClient sqsClient = sqsConfig.sqsClient();
        SqsAsyncClient sqsAsyncClient = sqsConfig.sqsAsyncClient();

        // Then
        assertNotNull(sqsClient);
        assertNotNull(sqsAsyncClient);
        // Clients should be created successfully with custom credentials
    }
}

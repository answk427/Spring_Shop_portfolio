package work.trade.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "file.storage.type", havingValue = "s3")
public class AwsConfig {

    @Bean
    public S3Client s3Client(
            @Value("${aws.credentials.access-key}") String accessKey,
            @Value("${aws.credentials.secret-key}") String secretKey,
            @Value("${aws.s3.region}") String region) {

        AwsBasicCredentials awsCredentials = AwsBasicCredentials
                .create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
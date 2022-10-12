package rocks.metaldetector.butler.supplier.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.services.s3.S3Client

import static software.amazon.awssdk.regions.Region.EU_CENTRAL_1

@Configuration
@Profile(["dev", "preview", "prod"])
class AwsConfig {

  @Bean
  S3Client s3Client(@Value('${aws.access-key}') String awsAccessKey,
                         @Value('${aws.secret-key}') String awsSecretKey) {
    return S3Client.builder()
        .credentialsProvider({ -> AwsBasicCredentials.create(awsAccessKey, awsSecretKey) })
        .region(EU_CENTRAL_1)
        .build()
  }
}

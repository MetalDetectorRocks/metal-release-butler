package rocks.metaldetector.butler.config.web

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

import static com.amazonaws.regions.Regions.EU_CENTRAL_1

@Configuration
@Profile(["dev", "preview", "prod"])
class AwsConfig {

  @Value('${aws.access-key}')
  String awsAccessKey

  @Value('${aws.secret-key}')
  String awsSecretKey

  @Bean
  AmazonS3 amazonS3Client() {
    AmazonS3ClientBuilder
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
        .withRegion(EU_CENTRAL_1)
        .build()
  }
}

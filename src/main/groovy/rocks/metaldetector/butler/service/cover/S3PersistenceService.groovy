package rocks.metaldetector.butler.service.cover

import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

import java.time.LocalDate

@Slf4j
@Service
@Profile(["preview", "prod"])
class S3PersistenceService implements CoverPersistenceService {

  static final String PATH = "images/"
  static final int EXPIRATION_PERIOD_IN_MONTHS = 6

  @Value('${aws.bucket-name}')
  String bucketName

  @Autowired
  AmazonS3 amazonS3Client

  @Override
  String persistCover(URL coverUrl) {
    def key = PATH + UUID.randomUUID() + "." + FilenameUtils.getExtension(coverUrl.getPath())
    def metadata = new ObjectMetadata(
        contentLength: coverUrl.openStream().bytes.length,
        contentType: fetchContentType(coverUrl),
        expirationTime: new Date(LocalDate.now().plusMonths(EXPIRATION_PERIOD_IN_MONTHS).toEpochDay()))
    try {
      amazonS3Client.putObject(bucketName, key, coverUrl.openStream(), metadata)
      return amazonS3Client.getUrl(bucketName, key).toExternalForm()
    }
    catch (SdkClientException ex) {
      log.warn("Could not upload cover from '${coverUrl}'", ex)
      return null
    }
  }

  private String fetchContentType(URL url) {
    def httpURLConnection = (HttpURLConnection) url.openConnection()
    httpURLConnection.setRequestMethod("HEAD")
    httpURLConnection.connect()
    return httpURLConnection.getContentType()
  }
}

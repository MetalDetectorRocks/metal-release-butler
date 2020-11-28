package rocks.metaldetector.butler.service.cover

import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Slf4j
@Service
@Profile(["dev", "preview", "prod"])
class S3PersistenceService implements CoverPersistenceService {

  public static final String PATH = "images/"

  @Value('${aws.s3-host}')
  String awsS3Host

  @Value('${aws.bucket-name}')
  String bucketName

  @Autowired
  AmazonS3 amazonS3Client

  @Override
  String persistCover(URL coverUrl) {
    def key = PATH + UUID.randomUUID() + "." + FilenameUtils.getExtension(coverUrl.getPath())
    try {
      log.info("Upload cover from '${coverUrl.toExternalForm()}'")
      PutObjectRequest request = createPutObjectRequest(key, coverUrl)
      amazonS3Client.putObject(request)

      URL s3Url = amazonS3Client.getUrl(bucketName, key)
      return "${awsS3Host}/${bucketName}${s3Url.path}"
    }
    catch (SdkClientException ex) {
      log.warn("Could not upload cover from '${coverUrl.toExternalForm()}'", ex)
      return null
    }
  }

  private createPutObjectRequest(String key, URL coverUrl) {
    return new PutObjectRequest(
            bucketName,
            key,
            coverUrl.openStream(),
            createObjectMetaData(coverUrl)
    ).withCannedAcl(CannedAccessControlList.PublicRead)
  }

  private ObjectMetadata createObjectMetaData(URL coverUrl) {
    return new ObjectMetadata(
            contentLength: coverUrl.openStream().bytes.length,
            contentType: fetchContentType(coverUrl)
    )
  }

  private String fetchContentType(URL url) {
    def httpURLConnection = (HttpURLConnection) url.openConnection()
    httpURLConnection.setRequestMethod("HEAD")
    httpURLConnection.connect()
    return httpURLConnection.getContentType()
  }
}

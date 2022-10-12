package rocks.metaldetector.butler.supplier.infrastructure.cover

import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import static software.amazon.awssdk.services.s3.model.ObjectCannedACL.PUBLIC_READ

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
  S3Client s3Client

  @Override
  String persistCover(URL coverUrl, String targetFolder) {
    def key = PATH + targetFolder + "/" + UUID.randomUUID() + "." + FilenameUtils.getExtension(coverUrl.getPath())
    try {
      log.info("Upload cover from '${coverUrl.toExternalForm()}'")
      PutObjectRequest request = createPutObjectRequest(key, coverUrl)
      RequestBody requestBody = RequestBody.fromBytes(coverUrl.openStream().bytes)
      s3Client.putObject(request, requestBody)

      URL s3Url = s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build())
      return "${awsS3Host}/${bucketName}${s3Url?.path}"
    }
    catch (AwsServiceException | SdkClientException ex) {
      log.warn("Could not find cover '${coverUrl.toExternalForm()}'. The upload to S3 is skipped.", ex)
      return null
    }
  }

  private PutObjectRequest createPutObjectRequest(String key, URL coverUrl) {
    return PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .acl(PUBLIC_READ)
        .metadata(createObjectMetaData(coverUrl))
        .build() as PutObjectRequest
  }

  private Map<String, String> createObjectMetaData(URL coverUrl) {
    return [contentLength: coverUrl.openStream().bytes.length.toString(),
            contentType  : fetchContentType(coverUrl)]
  }

  private String fetchContentType(URL url) {
    def httpURLConnection = (HttpURLConnection) url.openConnection()
    httpURLConnection.setRequestMethod("HEAD")
    httpURLConnection.connect()
    return httpURLConnection.getContentType()
  }
}

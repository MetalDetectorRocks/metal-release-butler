package rocks.metaldetector.butler.supplier.infrastructure.cover

import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Utilities
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import spock.lang.Specification
import spock.lang.Unroll

import static rocks.metaldetector.butler.supplier.infrastructure.cover.S3PersistenceService.PATH
import static software.amazon.awssdk.services.s3.model.ObjectCannedACL.PUBLIC_READ

class S3PersistenceServiceTest extends Specification {

  S3PersistenceService underTest = new S3PersistenceService(
      s3Client: Mock(S3Client),
      awsS3Host: "https://s3.eu-central-1.amazonaws.com",
      bucketName: "bucket"
  )

  UUID uuid = UUID.randomUUID()
  URL coverUrl
  URL responseUrl

  def setup() {
    coverUrl = GroovyMock(URL) {
      getPath() >> "/images/image.jpg"
      openStream() >> GroovyMock(InputStream) {
        bytes >> "bytes".bytes
      }
      openConnection() >> GroovyMock(HttpURLConnection) {
        contentType >> "image/jpeg"
      }
      toExternalForm() >> "http://www.internet.com"
    }

    responseUrl = GroovyMock(URL) {
      getPath() >> "/images/2021/1/image.jpg"
    }

    GroovyMock(UUID, global: true)
  }

  def "putObject: client is called with PutObjectRequest"() {
    given:
    def targetFolder = "path/to/target"
    UUID.randomUUID() >> uuid
    def expectedRequest = PutObjectRequest.builder()
        .bucket(underTest.bucketName)
        .key(PATH + targetFolder + "/" + uuid + ".jpg")
        .acl(PUBLIC_READ)
        .metadata([contentLength: coverUrl.openStream().bytes.length.toString(),
                   contentType  : "image/jpeg"])
        .build()
    S3Utilities utilities = GroovyMock(S3Utilities)
    underTest.s3Client.utilities() >> utilities
    utilities.getUrl(*_) >> responseUrl

    when:
    underTest.persistCover(coverUrl, targetFolder)

    then:
    1 * underTest.s3Client.putObject(expectedRequest, _)
  }

  def "putObject: client is called with RequestBody"() {
    given:
    GroovyMock(RequestBody, global: true)
    def targetFolder = "path/to/target"
    def expectedRequestBody = GroovyMock(RequestBody)
    UUID.randomUUID() >> uuid
    S3Utilities utilities = GroovyMock(S3Utilities)
    underTest.s3Client.utilities() >> utilities
    utilities.getUrl(*_) >> coverUrl

    when:
    underTest.persistCover(coverUrl, targetFolder)

    then:
    1 * RequestBody.fromBytes(coverUrl.openStream().bytes) >> expectedRequestBody

    and:
    1 * underTest.s3Client.putObject(_, expectedRequestBody)
  }

  @Unroll
  "if upload fails with '#exception' null is returned"() {
    given:
    underTest.s3Client.putObject(*_) >> { throw exception as Exception }

    expect:
    underTest.persistCover(coverUrl, "path/to/target") == null

    where:
    exception << [AwsServiceException.builder().build(), SdkClientException.builder().build(), S3Exception.builder().build()]
  }

  def "getUrl: utilities are called with bucket name"() {
    given:
    S3Utilities utilities = GroovyMock(S3Utilities)

    when:
    underTest.persistCover(coverUrl, "path/to/target")

    then:
    1 * underTest.s3Client.utilities() >> utilities

    then:
    1 * utilities.getUrl({ args -> args.bucket == underTest.bucketName }) >> responseUrl
  }

  def "getUrl: client is called with key"() {
    given:
    def key = "images/path/to/target/null.jpg"
    S3Utilities utilities = GroovyMock(S3Utilities)

    when:
    underTest.persistCover(coverUrl, "path/to/target")

    then:
    1 * underTest.s3Client.utilities() >> utilities

    then:
    1 * utilities.getUrl({ args -> args.key == key }) >> responseUrl
  }

  def "the image url is composed of the aws s3 host, the bucket name and the image path on s3"() {
    given:
    S3Utilities utilities = GroovyMock(S3Utilities)
    underTest.s3Client.utilities() >> utilities
    utilities.getUrl(*_) >> responseUrl

    expect:
    underTest.persistCover(coverUrl, "path/to/target") == "${underTest.awsS3Host}/${underTest.bucketName}${responseUrl.path}"
  }
}

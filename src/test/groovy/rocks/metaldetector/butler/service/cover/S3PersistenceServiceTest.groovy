package rocks.metaldetector.butler.service.cover

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import spock.lang.Specification
import spock.lang.Unroll

import static rocks.metaldetector.butler.service.cover.S3PersistenceService.PATH

class S3PersistenceServiceTest extends Specification {

  S3PersistenceService underTest = new S3PersistenceService(
          amazonS3Client: Mock(AmazonS3Client),
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
      toExternalForm() >> ""
    }

    responseUrl = GroovyMock(URL) {
      getPath() >> "/images/image.jpg"
    }

    underTest.amazonS3Client.getUrl(*_) >> responseUrl
    GroovyMock(UUID, global: true)
  }

  def "putObject: client is called with PutObjectRequest"() {
    given:
    UUID.randomUUID() >> uuid

    when:
    underTest.persistCover(coverUrl)

    then:
    1 * underTest.amazonS3Client.putObject({ arg ->
      assert arg.bucketName == underTest.bucketName
      assert arg.key == PATH + uuid + ".jpg"
      assert arg.inputStream == coverUrl.openStream()
      assert arg.metadata instanceof ObjectMetadata
      assert arg.cannedAcl == CannedAccessControlList.PublicRead
    })
  }

  def "putObject: client is called with content type, fetched via URLConnection, set in meta data"() {
    when:
    underTest.persistCover(coverUrl)

    then:
    1 * underTest.amazonS3Client.putObject({ arg ->
      assert arg.metadata.contentType == "image/jpeg"
    })
  }

  def "putObject: client is called with content length set in meta data"() {
    when:
    underTest.persistCover(coverUrl)

    then:
    1 * underTest.amazonS3Client.putObject({ arg ->
      assert arg.metadata.contentLength == "bytes".bytes.length
    })
  }

  @Unroll
  "if upload fails with '#exception.class' null is returned"() {
    given:
    underTest.amazonS3Client.putObject(*_) >> { throw exception }

    expect:
    underTest.persistCover(coverUrl) == null

    where:
    exception << [new SdkClientException("exception"), new AmazonServiceException("exeption")]
  }

  def "getUrl: client is called with bucket name"() {
    when:
    underTest.persistCover(coverUrl)

    then:
    1 * underTest.amazonS3Client.getUrl("bucket", _) >> responseUrl
  }

  def "getUrl: client is called with key"() {
    given:
    UUID.randomUUID() >> uuid

    when:
    underTest.persistCover(coverUrl)

    then:
    1 * underTest.amazonS3Client.getUrl(_, PATH + uuid + ".jpg") >> responseUrl
  }

  def "the image url is composed of the aws s3 host, the bucket name and the image path on s3"() {
    expect:
    underTest.persistCover(coverUrl) == "${underTest.awsS3Host}/${underTest.bucketName}${responseUrl.path}"
  }
}

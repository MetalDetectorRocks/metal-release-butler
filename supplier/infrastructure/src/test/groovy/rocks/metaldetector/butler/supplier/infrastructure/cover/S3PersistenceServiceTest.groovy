package rocks.metaldetector.butler.supplier.infrastructure.cover

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import spock.lang.Specification
import spock.lang.Unroll

import static rocks.metaldetector.butler.supplier.infrastructure.cover.S3PersistenceService.PATH

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
      toExternalForm() >> "http://www.internet.com"
    }

    responseUrl = GroovyMock(URL) {
      getPath() >> "/images/2021/1/image.jpg"
    }

    underTest.amazonS3Client.getUrl(*_) >> responseUrl
    GroovyMock(UUID, global: true)
  }

  def "putObject: client is called with PutObjectRequest"() {
    given:
    def targetFolder = "path/to/target"
    UUID.randomUUID() >> uuid

    when:
    underTest.persistCover(coverUrl, targetFolder)

    then:
    1 * underTest.amazonS3Client.putObject({ arg ->
      arg.bucketName == underTest.bucketName &&
      arg.key == PATH + targetFolder + "/" + uuid + ".jpg" &&
      arg.inputStream == coverUrl.openStream() &&
      arg.metadata instanceof ObjectMetadata &&
      arg.cannedAcl == CannedAccessControlList.PublicRead
    })
  }

  def "putObject: client is called with content type, fetched via URLConnection, set in meta data"() {
    when:
    underTest.persistCover(coverUrl, "path/to/target")

    then:
    1 * underTest.amazonS3Client.putObject({ arg ->
      assert arg.metadata.contentType == "image/jpeg"
    })
  }

  def "putObject: client is called with content length set in meta data"() {
    when:
    underTest.persistCover(coverUrl, "path/to/target")

    then:
    1 * underTest.amazonS3Client.putObject({ arg ->
      assert arg.metadata.contentLength == "bytes".bytes.length
    })
  }

  @Unroll
  "if upload fails with 'FileNotFoundException' null is returned"() {
    given:
    underTest.amazonS3Client.putObject(*_) >> { throw new FileNotFoundException() }

    expect:
    underTest.persistCover(coverUrl, "path/to/target") == null
  }

  def "getUrl: client is called with bucket name"() {
    when:
    underTest.persistCover(coverUrl, "path/to/target")

    then:
    1 * underTest.amazonS3Client.getUrl("bucket", _) >> responseUrl
  }

  def "getUrl: client is called with key"() {
    given:
    def targetFolder = "path/to/target"
    UUID.randomUUID() >> uuid

    when:
    underTest.persistCover(coverUrl, targetFolder)

    then:
    1 * underTest.amazonS3Client.getUrl(_, PATH + targetFolder + "/" + uuid + ".jpg") >> responseUrl
  }

  def "the image url is composed of the aws s3 host, the bucket name and the image path on s3"() {
    expect:
    underTest.persistCover(coverUrl, "path/to/target") == "${underTest.awsS3Host}/${underTest.bucketName}${responseUrl.path}"
  }
}

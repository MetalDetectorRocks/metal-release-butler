package rocks.metaldetector.butler.service.cover

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3Client
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.service.cover.S3PersistenceService.EXPIRATION_PERIOD_IN_MONTHS
import static rocks.metaldetector.butler.service.cover.S3PersistenceService.PATH

class S3PersistenceServiceTest extends Specification {

  S3PersistenceService underTest = new S3PersistenceService(amazonS3Client: Mock(AmazonS3Client),
                                                            bucketName: "bucket")
  URL requestUrl = new URL("http://www.internet.de/image.jpg")
  UUID uuid = UUID.randomUUID()
  URL responseUrl = new URL("http:www.noch-mehr-internet.de")

  def setup() {
    underTest.amazonS3Client.getUrl(*_) >> responseUrl
    GroovyMock(UUID, global: true)
  }

  def "the whole file path is returned"() {
    expect:
    underTest.persistCover(requestUrl) == responseUrl.toExternalForm()
  }

  def "putObject: client is called with bucket name"() {
    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.putObject("bucket", _, _, _)
  }

  def "putObject: client is called with key"() {
    given:
    UUID.randomUUID() >> uuid

    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.putObject(_, PATH + uuid + ".jpg", _, _)
  }

  def "putObject: client is called with input stream"() {
    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.putObject(_, _, { arg -> arg instanceof InputStream }, _)
  }

  def "putObject: client is called with content type, fetched via URLConnection, set in meta data"() {
    given:
    def mockUrl = GroovyMock(URL)
    def mockConnection = GroovyMock(HttpURLConnection)
    def mockInputStream = GroovyMock(InputStream)
    def contentType = "image/jpeg"
    mockUrl.openStream() >> mockInputStream
    mockUrl.openConnection() >> mockConnection
    mockConnection.getContentType() >> contentType
    mockInputStream.bytes >> "bytes".bytes

    when:
    underTest.persistCover(mockUrl)

    then:
    1 * underTest.amazonS3Client.putObject(_, _, _, { arg -> arg.contentType == contentType })
  }

  def "putObject: client is called with content length set in meta data"() {
    given:
    def mockUrl = GroovyMock(URL)
    def mockInputStream = GroovyMock(InputStream)
    mockUrl.openStream() >> mockInputStream
    mockUrl.openConnection() >> GroovyMock(HttpURLConnection)
    mockInputStream.bytes >> "bytes".bytes

    when:
    underTest.persistCover(mockUrl)

    then:
    1 * underTest.amazonS3Client.putObject(_, _, _, { arg -> arg.contentLength == "bytes".bytes.length })
  }

  def "putObject: client is called with expiration date set in meta data"() {
    given:
    def mockUrl = GroovyMock(URL)
    def mockInputStream = GroovyMock(InputStream)
    mockUrl.openStream() >> mockInputStream
    mockUrl.openConnection() >> GroovyMock(HttpURLConnection)
    mockInputStream.bytes >> "bytes".bytes

    when:
    underTest.persistCover(mockUrl)

    then:
    1 * underTest.amazonS3Client.putObject(_, _, _, { arg ->
      arg.expirationTime == new Date(LocalDate.now().plusMonths(EXPIRATION_PERIOD_IN_MONTHS).toEpochDay())
    })
  }

  @Unroll
  "if upload fails with '#exception.class' null is returned"() {
    given:
    underTest.amazonS3Client.putObject(*_) >> { throw exception }

    expect:
    underTest.persistCover(requestUrl) == null

    where:
    exception << [new SdkClientException("exception"), new AmazonServiceException("exeption")]
  }

  def "getUrl: client is called with bucket name"() {
    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.getUrl("bucket", _) >> responseUrl
  }

  def "getUrl: client is called with key"() {
    given:
    UUID.randomUUID() >> uuid

    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.getUrl(_, PATH + uuid + ".jpg") >> responseUrl
  }
}

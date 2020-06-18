package rocks.metaldetector.butler.service.cover

import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3Client
import spock.lang.Specification

import static rocks.metaldetector.butler.service.cover.S3PersistenceService.BUCKET_NAME
import static rocks.metaldetector.butler.service.cover.S3PersistenceService.PATH

class S3PersistenceServiceTest extends Specification {

  S3PersistenceService underTest = new S3PersistenceService(amazonS3Client: Mock(AmazonS3Client))
  URL requestUrl = new URL("http://www.internet.de/image.jpg")

  def "fileName with path is returned"() {
    expect:
    underTest.persistCover(requestUrl) == PATH + "image.jpg"
  }

  def "client is called with bucket name"() {
    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.putObject(BUCKET_NAME, _, _, _)
  }

  def "client is called with key"() {
    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.putObject(_, PATH + "image.jpg", _, _)
  }

  def "client is called with input stream"() {
    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.putObject(_, _, { arg -> arg instanceof InputStream }, _)
  }

  def "client is called without additional meta data"() {
    when:
    underTest.persistCover(requestUrl)

    then:
    1 * underTest.amazonS3Client.putObject(_, _, _, null)
  }

  def "if upload fails null is returned"() {
    given:
    underTest.amazonS3Client.putObject(*_) >> {throw new SdkClientException("exception")}

    expect:
    underTest.persistCover(requestUrl) == null
  }
}

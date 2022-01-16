package rocks.metaldetector.butler.web.rest

import org.springframework.core.io.ClassPathResource
import rocks.metaldetector.butler.service.release.ImageResourceFinder
import spock.lang.Specification

import java.nio.file.Paths

import static org.springframework.http.HttpStatus.OK

class ReleaseCoverRestControllerTest extends Specification {

  ReleaseCoverRestController underTest = new ReleaseCoverRestController(imageResourceFinder: Mock(ImageResourceFinder))

  def "should call image resource finder with image id"() {
    given:
    def coverId = "cover-id"

    when:
    underTest.getReleaseCover(coverId)

    then:
    1 * underTest.imageResourceFinder.findImage(Paths.get("images", coverId))
  }

  def "should return OK if image was found"() {
    given:
    underTest.imageResourceFinder.findImage(*_) >> new ClassPathResource("test.jpg")

    when:
    def result = underTest.getReleaseCover("cover-id")

    then:
    result.statusCode == OK
  }

  def "should return image resource from image resource finder"() {
    given:
    def imageResource = new ClassPathResource("test.jpg")
    underTest.imageResourceFinder.findImage(*_) >> imageResource

    when:
    def result = underTest.getReleaseCover("cover-id")

    then:
    byte[] resourceAsBytes = result.body.inputStream.bytes
    resourceAsBytes == imageResource.inputStream.bytes
  }
}

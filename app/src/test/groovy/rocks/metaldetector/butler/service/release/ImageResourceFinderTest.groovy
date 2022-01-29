package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.config.web.ResourceNotFoundException
import spock.lang.Specification

import java.nio.file.Paths

import static rocks.metaldetector.butler.service.release.ImageResourceFinder.ERROR_MESSAGE_DOTS
import static rocks.metaldetector.butler.service.release.ImageResourceFinder.ERROR_MESSAGE_EXTENSION
import static rocks.metaldetector.butler.service.release.ImageResourceFinder.ERROR_MESSAGE_NOT_FOUND
import static rocks.metaldetector.butler.service.release.ImageResourceFinder.VALID_FILE_EXTENSIONS

class ImageResourceFinderTest extends Specification {

  ImageResourceFinder underTest = new ImageResourceFinder()

  def "should return image resource"() {
    given:
    def location = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "test.jpg")

    when:
    def result = underTest.findImage(location)

    then:
    result
  }

  def "should throw ResourceNotFoundException if no image resource was found"() {
    given:
    def location = Paths.get("not-existing-image.jpg")

    when:
    underTest.findImage(location)

    then:
    def thrown = thrown(ResourceNotFoundException)
    thrown.message == ERROR_MESSAGE_NOT_FOUND
  }

  def "should throw exception if no image location is given"() {
    given:
    def location = Paths.get("test.txt")

    when:
    underTest.findImage(location)

    then:
    def thrown = thrown(ResourceNotFoundException)
    thrown.message == "$ERROR_MESSAGE_EXTENSION$VALID_FILE_EXTENSIONS"
  }

  def "should throw exception if image location contains two dots"() {
    given:
    def location = Paths.get("../i-am-outside-project-dir")

    when:
    underTest.findImage(location)

    then:
    def thrown = thrown(ResourceNotFoundException)
    thrown.message == ERROR_MESSAGE_DOTS
  }
}

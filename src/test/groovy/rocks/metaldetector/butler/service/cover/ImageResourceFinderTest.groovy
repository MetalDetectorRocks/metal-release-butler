package rocks.metaldetector.butler.service.cover

import spock.lang.Specification

import java.nio.file.Paths

class ImageResourceFinderTest extends Specification {

  ImageResourceFinder underTest = new ImageResourceFinder()

  def "should return image resource"() {
    given:
    def location = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "test.jpg")

    when:
    def result = underTest.findImage(location)

    then:
    result.isPresent()
  }

  def "should return empty optional if no image resource was found"() {
    given:
    def location = Paths.get("not-existing-image.jpg")

    when:
    def result = underTest.findImage(location)

    then:
    result.isEmpty()
  }

  def "should throw exception if no image location is given"() {
    given:
    def location = Paths.get("test.txt")

    when:
    underTest.findImage(location)

    then:
    thrown(IllegalArgumentException)
  }

  def "should throw exception if image location contains two dots"() {
    given:
    def location = Paths.get("../i-am-outside-project-dir")

    when:
    underTest.findImage(location)

    then:
    thrown(IllegalArgumentException)
  }
}

package rocks.metaldetector.butler.supplier.timeformetal.cover

import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverPersistenceService
import spock.lang.Specification

class TimeForMetalCoverServiceTest extends Specification {

  TimeForMetalCoverService underTest = new TimeForMetalCoverService(coverPersistenceService: Mock(CoverPersistenceService))

  def "if url is not null persistenceService is called and result returned"() {
    given:
    def sourceUrl = "http://www.internet.com"
    def targetFolder = "path/to/target"
    def expectedPath = "path/to/image"

    when:
    def result = underTest.transfer(sourceUrl, targetFolder)

    then:
    1 * underTest.coverPersistenceService.persistCover(new URL(sourceUrl), targetFolder) >> expectedPath

    and:
    result == expectedPath
  }

  def "thumb url is converted to full size url"() {
    given:
    def sourceUrl = "http://www.internet.com?bla-100x100.jpg"
    def expectedUrl = "http://www.internet.com?bla.jpg"

    when:
    underTest.transfer(sourceUrl, "path/to/target")

    then:
    1 * underTest.coverPersistenceService.persistCover(new URL(expectedUrl), _)
  }

  def "if source url is null nothing is called and null returned"() {
    when:
    def result = underTest.transfer(null, "path/to/target")

    then:
    0 * underTest.coverPersistenceService.persistCover(*_)

    and:
    !result
  }
}

package rocks.metaldetector.butler.service.cover

import spock.lang.Specification

class TimeForMetalCoverServiceTest extends Specification {

  TimeForMetalCoverService underTest = new TimeForMetalCoverService(coverPersistenceService: Mock(CoverPersistenceService))

  def "if url is not null persistenceService is called and result returned"() {
    given:
    def sourceUrl = "http://www.internet.com"
    def expectedPath = "path/to/image"

    when:
    def result = underTest.transfer(sourceUrl)

    then:
    1 * underTest.coverPersistenceService.persistCover(new URL(sourceUrl)) >> expectedPath

    and:
    result == expectedPath
  }

  def "thumb url is converted to full size url"() {
    given:
    def sourceUrl = "http://www.internet.com?bla-100x100.jpg"
    def expectedUrl = "http://www.internet.com?bla.jpg"
    def expectedPath = "path/to/image"

    when:
    def result = underTest.transfer(sourceUrl)

    then:
    1 * underTest.coverPersistenceService.persistCover(new URL(expectedUrl)) >> expectedPath

    and:
    result == expectedPath
  }

  def "if source url is null nothing is called and null returned"() {
    when:
    def result = underTest.transfer(null)

    then:
    0 * underTest.coverPersistenceService.persistCover(*_)

    and:
    !result
  }
}

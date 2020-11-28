package rocks.metaldetector.butler.service.cover

import spock.lang.Specification

class MetalArchivesCoverServiceTest extends Specification {

  MetalArchivesCoverService underTest = new MetalArchivesCoverService(metalArchivesCoverFetcher: Mock(CoverFetcher),
                                                                      coverPersistenceService: Mock(CoverPersistenceService))

  def "coverFetcher is called to get the url of the release cover"() {
    given:
    def sourceUrl = "http://www.internet.com"

    when:
    underTest.transfer(sourceUrl)

    then:
    1 * underTest.metalArchivesCoverFetcher.fetchCoverUrl(sourceUrl)
  }

  def "if url is returned persistenceService is called and result returned"() {
    given:
    def sourceUrl = "http://www.internet.com"
    underTest.metalArchivesCoverFetcher.fetchCoverUrl(sourceUrl) >> sourceUrl
    def expectedPath = "path/to/image"

    when:
    def result = underTest.transfer(sourceUrl)

    then:
    1 * underTest.coverPersistenceService.persistCover(new URL(sourceUrl)) >> expectedPath

    and:
    result == expectedPath
  }

  def "if no url is returned persistenceService is not called and null returned"() {
    given:
    def sourceUrl = "http://www.internet.com"

    when:
    def result = underTest.transfer(sourceUrl)

    then:
    0 * underTest.coverPersistenceService.persistCover(*_)

    and:
    !result
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

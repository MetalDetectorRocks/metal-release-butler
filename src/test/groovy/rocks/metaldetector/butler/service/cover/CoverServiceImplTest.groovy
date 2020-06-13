package rocks.metaldetector.butler.service.cover

import spock.lang.Specification

class CoverServiceImplTest extends Specification {

  CoverServiceImpl underTest = new CoverServiceImpl(metalArchivesCoverFetcher: Mock(CoverFetcher),
                                                    localCoverPersistenceService: Mock(CoverPersistenceService))

  def "coverFetcher is called to get the url of the release cover"() {
    given:
    def sourceUrl = new URL("http://www.internet.com")

    when:
    underTest.transfer(sourceUrl)

    then:
    1 * underTest.metalArchivesCoverFetcher.fetchCoverUrl(sourceUrl)
  }

  def "if url is returned persistenceService is called and result returned"() {
    given:
    def sourceUrl = new URL("http://www.internet.com")
    underTest.metalArchivesCoverFetcher.fetchCoverUrl(sourceUrl) >> sourceUrl
    def expectedPath = "path/to/image"

    when:
    def result = underTest.transfer(sourceUrl)

    then:
    1 * underTest.localCoverPersistenceService.persistCover(sourceUrl) >> expectedPath

    and:
    result == expectedPath
  }

  def "if no url is returned persistenceService is not called and null returned"() {
    given:
    def sourceUrl = new URL("http://www.internet.com")

    when:
    def result = underTest.transfer(sourceUrl)

    then:
    0 * underTest.localCoverPersistenceService.persistCover(*_)

    and:
    !result
  }
}

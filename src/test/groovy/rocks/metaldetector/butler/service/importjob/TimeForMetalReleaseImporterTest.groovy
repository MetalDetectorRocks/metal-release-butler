package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.Converter
import rocks.metaldetector.butler.service.cover.CoverService
import rocks.metaldetector.butler.supplier.timeformetal.TimeForMetalWebCrawler
import spock.lang.Specification

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory

class TimeForMetalReleaseImporterTest extends Specification {

  TimeForMetalReleaseImporter underTest = new TimeForMetalReleaseImporter(
          webCrawler: Mock(TimeForMetalWebCrawler),
          timeForMetalReleaseEntityConverter: Mock(Converter),
          timeForMetalCoverService: Mock(CoverService),
          releaseRepository: Mock(ReleaseRepository)
  )

  def "webCrawler is called with initial page"() {
    given:
    underTest.timeForMetalReleaseEntityConverter.convert(*_) >> []

    when:
    underTest.importReleases()

    then:
    1 * underTest.webCrawler.requestReleases(1)
  }

  def "entityConverter is called with raw release page"() {
    given:
    def rawPage = "releasePage"
    underTest.webCrawler.requestReleases(*_) >> rawPage

    when:
    underTest.importReleases()

    then:
    1 * underTest.timeForMetalReleaseEntityConverter.convert(rawPage) >> []
  }

  def "if entityConverter returns new releases webCrawler is called with next page"() {
    given:
    def firstReleasePage = "releasePage"
    underTest.timeForMetalReleaseEntityConverter.convert(firstReleasePage) >> [ReleaseEntityFactory.createReleaseEntity("a")]
    underTest.timeForMetalReleaseEntityConverter.convert(null) >> []
    underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >> true

    when:
    underTest.importReleases()

    then:
    1 * underTest.webCrawler.requestReleases(1) >> firstReleasePage

    then:
    1 * underTest.webCrawler.requestReleases(2) >> null
  }

  def "should return specific cover service"() {
    expect:
    underTest.timeForMetalCoverService == underTest.getCoverService()
  }
}

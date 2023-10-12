package rocks.metaldetector.butler.supplier.timeformetal.importjob

import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import rocks.metaldetector.butler.supplier.infrastructure.converter.Converter
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService
import rocks.metaldetector.butler.supplier.timeformetal.TimeForMetalWebCrawler
import spock.lang.Specification

import java.util.concurrent.locks.ReentrantReadWriteLock

import static rocks.metaldetector.butler.supplier.timeformetal.DtoFactory.ReleaseEntityFactory

class TimeForMetalReleaseImporterTest extends Specification {

  TimeForMetalReleaseImporter underTest = new TimeForMetalReleaseImporter(
      webCrawler: Mock(TimeForMetalWebCrawler),
      timeForMetalReleaseEntityConverter: Mock(Converter),
      timeForMetalCoverService: Mock(CoverService),
      releaseRepository: Mock(ReleaseRepository),
      reentrantReadWriteLock: Mock(ReentrantReadWriteLock)
  )

  def "webCrawler is called with initial page"() {
    given:
    underTest.reentrantReadWriteLock.readLock() >> Mock(ReentrantReadWriteLock.ReadLock)
    underTest.reentrantReadWriteLock.writeLock() >> Mock(ReentrantReadWriteLock.WriteLock)
    underTest.timeForMetalReleaseEntityConverter.convert(*_) >> []
    underTest.releaseRepository.saveAll(*_) >> []

    when:
    underTest.importReleases()

    then:
    1 * underTest.webCrawler.requestReleases(1)
  }

  def "entityConverter is called with raw release page"() {
    given:
    underTest.reentrantReadWriteLock.readLock() >> Mock(ReentrantReadWriteLock.ReadLock)
    underTest.reentrantReadWriteLock.writeLock() >> Mock(ReentrantReadWriteLock.WriteLock)
    def rawPage = "releasePage"
    underTest.webCrawler.requestReleases(*_) >> rawPage
    underTest.releaseRepository.saveAll(*_) >> []

    when:
    underTest.importReleases()

    then:
    1 * underTest.timeForMetalReleaseEntityConverter.convert(rawPage) >> []
  }

  def "if entityConverter returns new releases webCrawler is called with next page"() {
    given:
    underTest.reentrantReadWriteLock.readLock() >> Mock(ReentrantReadWriteLock.ReadLock)
    underTest.reentrantReadWriteLock.writeLock() >> Mock(ReentrantReadWriteLock.WriteLock)
    def firstReleasePage = "releasePage"
    underTest.timeForMetalReleaseEntityConverter.convert(firstReleasePage) >> [ReleaseEntityFactory.createReleaseEntity("a")]
    underTest.timeForMetalReleaseEntityConverter.convert(null) >> []
    underTest.releaseRepository.existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(*_) >> true
    underTest.releaseRepository.saveAll(*_) >> []

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

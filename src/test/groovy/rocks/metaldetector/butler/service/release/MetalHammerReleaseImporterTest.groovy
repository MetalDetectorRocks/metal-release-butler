package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter
import rocks.metaldetector.butler.service.importjob.ImportResult
import rocks.metaldetector.butler.service.importjob.MetalHammerReleaseImporter
import rocks.metaldetector.butler.supplier.metalhammer.MetalHammerWebCrawler
import spock.lang.Specification

import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

class MetalHammerReleaseImporterTest extends Specification {

  MetalHammerReleaseImporter underTest = new MetalHammerReleaseImporter(
      webCrawler: Mock(MetalHammerWebCrawler),
      metalHammerReleaseEntityConverter: Mock(MetalHammerReleaseEntityConverter),
      releaseRepository: Mock(ReleaseRepository)
  )

  def "web crawler is called"() {
    given:
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> []

    when:
    underTest.importReleases()

    then:
    1 * underTest.webCrawler.requestReleases() >> "page"
  }

  def "releaseEntityConverter is called with web crawlers response"() {
    given:
    def responsePage = "page"
    underTest.webCrawler.requestReleases() >> responsePage

    when:
    underTest.importReleases()

    then:
    1 * underTest.metalHammerReleaseEntityConverter.convert(responsePage) >> []
  }

  def "releaseRepository checks if each release already exists"() {
    given:
    def release1 = new ReleaseEntity(artist: "Darkthrone")
    def release2 = new ReleaseEntity(artist: "Mayhem")
    def releases = [release1, release2]
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> releases

    when:
    underTest.importReleases()

    then:
    1 * underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(release1.artist, release1.albumTitle, release1.releaseDate)

    and:
    1 * underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(release2.artist, release2.albumTitle, release2.releaseDate)
  }

  def "each new release is saved"() {
    given:
    def release1 = new ReleaseEntity(artist: "Darkthrone")
    def release2 = new ReleaseEntity(artist: "Mayhem")
    def releases = [release1, release2]
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> releases
    underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(*_) >>> [true, false]

    when:
    underTest.importReleases()

    then:
    1 * underTest.releaseRepository.save({ args ->
      assert args == release2
    })
  }

  def "should return ImportResult  with correct values for 'totalCountRequested' and 'totalCountImported'"() {
    given:
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> [new ReleaseEntity(artist: "Darkthrone"), new ReleaseEntity(artist: "Mayhem")]
    underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(*_) >>> [true, false]

    when:
    def importResult = underTest.importReleases()

    then:
    importResult == new ImportResult(totalCountRequested: 2, totalCountImported: 1)
  }

  def "Duplicates are filtered out before the database query checks whether the release already exists"() {
    given:
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> [
            new ReleaseEntity(artist: "Darkthrone", albumTitle: "Transilvanian Hunger", releaseDate: LocalDate.of(1994, 10, 10)),
            new ReleaseEntity(artist: "Darkthrone", albumTitle: "Transilvanian Hunger", releaseDate: LocalDate.of(1994, 10, 10)),
            new ReleaseEntity(artist: "Darkthrone", albumTitle: "Panzerfaust", releaseDate: LocalDate.of(1994, 10, 10))
    ]

    when:
    underTest.importReleases()

    then:
    2 * underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(*_)
  }

  def "should return METAL_HAMMER_DE as release source"() {
    expect:
    underTest.getReleaseSource() == METAL_HAMMER_DE
  }
}

package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter
import rocks.metaldetector.butler.service.cover.HTTPBuilderFunction
import rocks.metaldetector.butler.service.transformer.ImportJobTransformer
import rocks.metaldetector.butler.supplier.metalhammer.MetalHammerRestClient
import rocks.metaldetector.butler.web.dto.ImportJobResponse
import spock.lang.Specification

class MetalHammerReleaseImporterTest extends Specification {

  MetalHammerReleaseImporter underTest = new MetalHammerReleaseImporter(
      restClient: Mock(MetalHammerRestClient),
      httpBuilderFunction: Mock(HTTPBuilderFunction),
      metalHammerReleaseEntityConverter: Mock(MetalHammerReleaseEntityConverter),
      importJobRepository: Mock(ImportJobRepository),
      importJobTransformer: Mock(ImportJobTransformer),
      releaseRepository: Mock(ReleaseRepository))

  def setup() {
    underTest.importJobRepository.findById(*_) >> Optional.of(new ImportJobEntity())
  }

  def "restClient is called"() {
    given:
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> []

    when:
    underTest.importReleases()

    then:
    1 * underTest.restClient.requestReleases() >> "page"
  }

  def "releaseEntityConverter is called with restClient's response"() {
    given:
    def responsePage = "page"
    underTest.restClient.requestReleases() >> responsePage

    when:
    underTest.importReleases()

    then:
    1 * underTest.metalHammerReleaseEntityConverter.convert(responsePage) >> []
  }

  def "releaseRepository checks if each release already exsits"() {
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

  def "should update import job with correct values for 'totalCountRequested' and 'totalCountImported'"() {
    given:
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> [new ReleaseEntity(artist: "Darkthrone"), new ReleaseEntity(artist: "Darkthrone")]
    underTest.releaseRepository.existsByArtistAndAlbumTitleAndReleaseDate(*_) >>> [true, false]

    when:
    underTest.importReleases()

    then:
    1 * underTest.importJobRepository.save({ args ->
      assert args.totalCountRequested == 2
      assert args.totalCountImported == 1
      assert args.endTime
    })
  }

  def "should use ImportJobTransformer to transform ImportJobEntity that is used as result"() {
    given:
    def importJobEntityMock = new ImportJobEntity()
    def transformedResponse = new ImportJobResponse()
    underTest.metalHammerReleaseEntityConverter.convert(*_) >> [new ReleaseEntity()]
    underTest.importJobRepository.save(*_) >> importJobEntityMock

    when:
    def result = underTest.importReleases()

    then:
    1 * underTest.importJobTransformer.transform(importJobEntityMock) >> transformedResponse

    and:
    result == transformedResponse
  }
}

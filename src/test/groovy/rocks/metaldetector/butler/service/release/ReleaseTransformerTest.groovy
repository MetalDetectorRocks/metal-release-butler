package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.model.release.ReleaseEntity
import spock.lang.Specification

import java.time.LocalDate

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
class ReleaseTransformerTest extends Specification {

  ReleaseTransformer underTest = new ReleaseTransformer()
  ReleaseEntity releaseEntity = ReleaseEntityFactory.createReleaseEntity("A", LocalDate.now())

  def "should transform releaseEntity to releaseDto"() {
    when:
    def result = underTest.transform(releaseEntity)

    then:
    result.id == releaseEntity.id
    result.artist == releaseEntity.artist
    result.albumTitle == releaseEntity.albumTitle
    result.releaseDate == releaseEntity.releaseDate
    result.estimatedReleaseDate == releaseEntity.estimatedReleaseDate
    result.source == releaseEntity.source
    result.genre == releaseEntity.genre
    result.releaseDetailsUrl == releaseEntity.releaseDetailsUrl
    result.artistDetailsUrl == releaseEntity.artistDetailsUrl
    result.type == releaseEntity.type
    result.coverUrl == releaseEntity.coverUrl
    result.state == releaseEntity.state
    result.additionalArtists == releaseEntity.additionalArtists
  }

  def "urls can be null"() {
    given:
    def releaseEntity = new ReleaseEntity(source: METAL_ARCHIVES)

    when:
    def result = underTest.transform(releaseEntity)

    then:
    noExceptionThrown()

    and:
    !result.artistDetailsUrl
    !result.releaseDetailsUrl
  }
}

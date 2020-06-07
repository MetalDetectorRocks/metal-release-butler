package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.cover.CoverService
import spock.lang.Specification

class PersistReleaseEntityTaskTest extends Specification {

  PersistReleaseEntityTask underTest = new PersistReleaseEntityTask(
          coverService: Mock(CoverService),
          releaseEntity: Mock(ReleaseEntity),
          releaseRepository: Mock(ReleaseRepository)
  )

  def "should pass 'metalArchivesAlbumUrl' to cover service"() {
    given:
    def metalArchivesAlbumUrl = new URL("http://cover-url")
    underTest.releaseEntity.metalArchivesAlbumUrl >> metalArchivesAlbumUrl

    when:
    underTest.run()

    then:
    1 * underTest.coverService.transfer(metalArchivesAlbumUrl)
  }

  def "should set result from cover service cover url on release entity"() {
    given:
    def coverUrl = "http://cover-url"
    underTest.coverService.transfer(*_) >> coverUrl

    when:
    underTest.run()

    then:
    1 * underTest.releaseEntity.setCoverUrl(coverUrl)
  }

  def "should save release entity"() {
    when:
    underTest.run()

    then:
    1 * underTest.releaseRepository.save(underTest.releaseEntity)
  }
}

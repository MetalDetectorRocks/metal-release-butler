package rocks.metaldetector.butler.service.importjob

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

  def "should pass 'albumUrl' to cover service"() {
    given:
    def albumUrl = "http://cover-url"
    underTest.releaseEntity.coverSourceUrl >> albumUrl

    when:
    underTest.run()

    then:
    1 * underTest.coverService.transfer(albumUrl)
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

  def "cover not saved if cover service not set"() {
    given:
    underTest.coverService = null

    when:
    underTest.run()

    then:
    0 * underTest.coverService.transfer(*_)

    and:
    0 * underTest.releaseEntity.setCoverUrl(*_)
  }
}

package rocks.metaldetector.butler.supplier.infrastructure.importjob

import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService
import spock.lang.Specification

import java.time.LocalDate

class CoverTransferTaskTest extends Specification {

  CoverTransferTask underTest = new CoverTransferTask(
      coverService: Mock(CoverService),
      releaseEntity: Mock(ReleaseEntity)
  )

  def "should pass 'albumUrl' to cover service"() {
    given:
    def albumUrl = "http://cover-url"
    underTest.releaseEntity.releaseDetailsUrl >> albumUrl

    when:
    underTest.run()

    then:
    1 * underTest.coverService.transfer(albumUrl, _)
  }

  def "should compose target folder of release date"() {
    given:
    underTest.releaseEntity.releaseDate >> LocalDate.of(2021, 1, 1)

    when:
    underTest.run()

    then:
    1 * underTest.coverService.transfer(_, "2021/1")
  }

  def "should compose target folder of estimated release date if release date is null"() {
    given:
    def estimatedReleaseDate = "Winter 2021"
    underTest.releaseEntity.releaseDate >> null
    underTest.releaseEntity.estimatedReleaseDate >> estimatedReleaseDate

    when:
    underTest.run()

    then:
    1 * underTest.coverService.transfer(_, estimatedReleaseDate)
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
}

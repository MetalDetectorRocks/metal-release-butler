package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.service.cover.CoverService
import spock.lang.Specification

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
}

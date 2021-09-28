package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.supplier.infrastructure.ReleaseEntityDeleteRequestEvent
import spock.lang.Specification

class ReleaseEntityDeleteRequestListenerTest extends Specification {

  ReleaseEntityDeleteRequestListener underTest = new ReleaseEntityDeleteRequestListener(
          releaseService: Mock(ReleaseService)
  )

  def "should call pass event parameter to release service"() {
    given:
    def releaseDetailsUrl = "release-details-url"
    def event = new ReleaseEntityDeleteRequestEvent(this, releaseDetailsUrl)

    when:
    underTest.onApplicationEvent(event)

    then:
    1 * underTest.releaseService.deleteByReleaseDetailsUrl(releaseDetailsUrl)
  }
}

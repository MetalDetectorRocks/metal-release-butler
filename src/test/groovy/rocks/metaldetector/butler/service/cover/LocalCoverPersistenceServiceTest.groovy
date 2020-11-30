package rocks.metaldetector.butler.service.cover

import spock.lang.Specification

import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASE_IMAGES

class LocalCoverPersistenceServiceTest extends Specification {

  LocalCoverPersistenceService underTest = new LocalCoverPersistenceService(
          fileTransferService: Mock(LocalFileTransferService)
  )

  def "should run without error (smoke test)"() {
    given:
    def url = GroovyMock(URL) {
      getPath() >> "foo.jpg"
    }

    underTest.fileTransferService.transferFileFromUrl(*_) >> 1

    when:
    def result = underTest.persistCover(url)

    then:
    result.startsWith(RELEASE_IMAGES)

    and:
    result.endsWith("jpg")
  }
}

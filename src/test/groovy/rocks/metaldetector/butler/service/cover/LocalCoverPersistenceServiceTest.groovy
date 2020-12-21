package rocks.metaldetector.butler.service.cover

import spock.lang.Specification

import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASE_IMAGES

class LocalCoverPersistenceServiceTest extends Specification {

  LocalCoverPersistenceService underTest = new LocalCoverPersistenceService(
          fileTransferService: Mock(LocalFileTransferService)
  )

  def "should run without error (smoke test)"() {
    given:
    def targetFolder = "path/to/target"
    def url = GroovyMock(URL) {
      getPath() >> "foo.jpg"
    }

    underTest.fileTransferService.transferFileFromUrl(*_) >> 1

    when:
    def result = underTest.persistCover(url, targetFolder)

    then:
    result.startsWith("$RELEASE_IMAGES?id=$targetFolder")

    and:
    result.endsWith("jpg")
  }
}

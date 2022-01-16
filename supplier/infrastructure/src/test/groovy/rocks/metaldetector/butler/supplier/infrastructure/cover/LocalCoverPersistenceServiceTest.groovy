package rocks.metaldetector.butler.supplier.infrastructure.cover

import spock.lang.Specification

import static rocks.metaldetector.butler.supplier.infrastructure.Endpoints.RELEASE_IMAGES

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

  def "null is returned if empty file is created"() {
    given:
    def targetFolder = "path/to/target"
    def url = GroovyMock(URL) {
      getPath() >> "foo.jpg"
    }
    underTest.fileTransferService.transferFileFromUrl(*_) >> 0

    when:
    def result = underTest.persistCover(url, targetFolder)

    then:
    !result
  }
}

package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.importjob.ReleaseImporter
import rocks.metaldetector.butler.service.importjob.ImportJobTransformer
import rocks.metaldetector.butler.web.dto.ImportJobResponse
import spock.lang.Specification

class ReleaseImporterTest extends Specification {

  ReleaseImporter underTest = new ReleaseImporter() {
    @Override
    ImportJobResponse importReleases(Long internalJobId) {
      updateImportJob(internalJobId, 1, 1)
      return null
    }
  }

  void setup() {
    underTest.setImportJobRepository(Mock(ImportJobRepository))
    underTest.setReleaseRepository(Mock(ReleaseRepository))
    underTest.setImportJobTransformer(Mock(ImportJobTransformer))
  }

  def "should update the corresponding import job"() {
    given:
    def internalJobId = 666
    def importJobEntityMock = new ImportJobEntity()

    when:
    underTest.importReleases(internalJobId)

    then:
    1 * underTest.importJobRepository.findById(internalJobId) >> Optional.of(importJobEntityMock)

    then:
    1 * underTest.importJobRepository.save({ args ->
      assert args.totalCountRequested == 1
      assert args.totalCountImported == 1
      assert args.endTime
    })
  }
}

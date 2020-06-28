package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.web.dto.CreateImportJobResponse
import spock.lang.Specification

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

class ImportJobServiceImplTest extends Specification {

  ImportJobServiceImpl underTest = new ImportJobServiceImpl(\
          importJobRepository: Mock(ImportJobRepository),
          metalArchivesReleaseImportService: Mock(MetalArchivesReleaseImporter),
          metalHammerReleaseImportService: Mock(MetalHammerReleaseImporter)
  )

  def "importFromExternalSources: should create a new import job before start importing new releases"() {
    when:
    underTest.importFromExternalSources()

    then:
    1 * underTest.importJobRepository.save({
      assert it.jobId != null
      assert it.startTime != null
      assert it.source == METAL_ARCHIVES
    }) >> new ImportJobEntity(jobId: UUID.randomUUID())

    then:
    1 * underTest.metalArchivesReleaseImportService.importReleases(*_)

    then:
    1 * underTest.importJobRepository.save({
      assert it.jobId != null
      assert it.startTime != null
      assert it.source == METAL_HAMMER_DE
    }) >> new ImportJobEntity(jobId: UUID.randomUUID())

    then:
    1 * underTest.metalHammerReleaseImportService.importReleases(*_)
  }

  def "importFromExternalSources: should pass internal import job id to metalArchivesReleaseImportService"() {
    given:
    Long id = 666
    underTest.importJobRepository.save(*_) >> new ImportJobEntity(id: id)

    when:
    underTest.importFromExternalSources()

    then:
    1 * underTest.metalArchivesReleaseImportService.importReleases(id)

    and:
    1 * underTest.metalHammerReleaseImportService.importReleases(id)
  }

  def "importFromExternalSources: should return response with import job id"() {
    given:
    UUID jobId = UUID.randomUUID()
    underTest.importJobRepository.save(*_) >> new ImportJobEntity(jobId: jobId)

    when:
    def result = underTest.importFromExternalSources()

    then:
    result == new CreateImportJobResponse(jobIds: [jobId, jobId])
  }

  // ToDo DanielW: Test
}

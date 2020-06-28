package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.web.dto.CreateImportJobResponse
import spock.lang.Specification

import static rocks.metaldetector.butler.DtoFactory.*
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

class ImportJobServiceImplTest extends Specification {

  ImportJobServiceImpl underTest = new ImportJobServiceImpl(\
          importJobRepository: Mock(ImportJobRepository),
          importJobTransformer: Mock(ImportJobTransformer),
          metalArchivesReleaseImportService: Mock(MetalArchivesReleaseImporter),
          metalHammerReleaseImportService: Mock(MetalHammerReleaseImporter)
  )

  def "should call import job repository"() {
    when:
    underTest.findAllImportJobResults()

    then:
    1 * underTest.importJobRepository.findAll()
  }

  def "should transform each job entity with job transformer"() {
    given:
    def jobEntities = [
            ImportJobEntityFactory.createImportJobEntity(),
            ImportJobEntityFactory.createImportJobEntity()
    ]
    underTest.importJobRepository.findAll() >> jobEntities

    when:
    underTest.findAllImportJobResults()

    then:
    1 * underTest.importJobTransformer.transform(jobEntities[0])

    then:
    1 * underTest.importJobTransformer.transform(jobEntities[1])
  }

  def "should return list of transformed import jobs"() {
    given:
    def jobEntities = [
            ImportJobEntityFactory.createImportJobEntity(),
            ImportJobEntityFactory.createImportJobEntity()
    ]
    underTest.importJobRepository.findAll() >> jobEntities

    when:
    def results = underTest.findAllImportJobResults()

    then:
    results.size() == jobEntities.size()
  }

  def "should create a new import job before start importing new releases"() {
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

  def "should pass internal import job id to metalArchivesReleaseImportService"() {
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

  def "should return response with import job id"() {
    given:
    UUID jobId = UUID.randomUUID()
    underTest.importJobRepository.save(*_) >> new ImportJobEntity(jobId: jobId)

    when:
    def result = underTest.importFromExternalSources()

    then:
    result == new CreateImportJobResponse(jobIds: [jobId, jobId])
  }
}

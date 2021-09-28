package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobEntity
import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobRepository
import rocks.metaldetector.butler.service.importjob.ImportJobTransformer
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ImportResult
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ReleaseImporter
import spock.lang.Specification

import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.ERROR
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.RUNNING
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.SUCCESSFUL
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TIME_FOR_METAL
import static rocks.metaldetector.butler.testutil.DtoFactory.ImportJobEntityFactory

class ImportJobServiceTest extends Specification {

  ImportJobService underTest = new ImportJobService(
      importJobRepository: Mock(ImportJobRepository),
      importJobTransformer: Mock(ImportJobTransformer)
  )

  def "findAllImportJobResults: should call import job repository"() {
    when:
    underTest.findAllImportJobResults()

    then:
    1 * underTest.importJobRepository.findAll()
  }

  def "findAllImportJobResults: should transform each job entity with job transformer"() {
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

  def "findAllImportJobResults: should return list of transformed import jobs"() {
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

  def "importFromExternalSources: should create a new import job, invoking 'importReleases()' and update the import job in this order"() {
    given:
    def releaseImporterMock = Mock(ReleaseImporter)
    underTest.releaseImporters = [releaseImporterMock]
    releaseImporterMock.releaseSource >> METAL_ARCHIVES
    ImportResult importResult = new ImportResult(totalCountRequested: 10, totalCountImported: 5)
    ImportJobEntity metalArchivesImportJob = new ImportJobEntity(jobId: UUID.randomUUID())

    when:
    underTest.importFromExternalSources()

    then:
    1 * underTest.importJobRepository.save({
      assert it.jobId != null
      assert it.startTime != null
      assert it.source == METAL_ARCHIVES
    }) >> metalArchivesImportJob

    then:
    1 * releaseImporterMock.importReleases() >> importResult

    then:
    1 * underTest.importJobRepository.save({ args ->
      assert args.jobId == metalArchivesImportJob.jobId
      assert args.totalCountRequested == importResult.totalCountRequested
      assert args.totalCountImported == importResult.totalCountImported
      assert args.state == SUCCESSFUL
      assert args.endTime
    })
  }

  def "importFromExternalSources: should handle any exception and update the import job with state ERROR"() {
    given:
    def releaseImporterMock = Mock(ReleaseImporter)
    underTest.releaseImporters = [releaseImporterMock]
    releaseImporterMock.releaseSource >> METAL_ARCHIVES
    ImportJobEntity metalArchivesImportJob = new ImportJobEntity(jobId: UUID.randomUUID())
    underTest.importJobRepository.save(*_) >> metalArchivesImportJob
    releaseImporterMock.importReleases() >> { throw new RuntimeException("boom") }

    when:
    underTest.importFromExternalSources()

    then:
    1 * underTest.importJobRepository.save({ args ->
      assert args.jobId == metalArchivesImportJob.jobId
      assert args.totalCountRequested == null
      assert args.totalCountImported == null
      assert args.state == ERROR
      assert args.endTime
    })

    and:
    noExceptionThrown()
  }

  def "importFromExternalSources: should process the release importers according to the specified order"() {
    given:
    def releaseImporterMock1 = Mock(ReleaseImporter)
    def releaseImporterMock2 = Mock(ReleaseImporter)
    underTest.releaseImporters = [releaseImporterMock1, releaseImporterMock2]
    releaseImporterMock1.releaseSource >> METAL_ARCHIVES
    releaseImporterMock2.releaseSource >> TIME_FOR_METAL
    underTest.importJobRepository.save(*_) >> new ImportJobEntity()

    when:
    underTest.importFromExternalSources()

    then:
    1 * releaseImporterMock1.importReleases() >> new ImportResult()

    then:
    1 * releaseImporterMock2.importReleases() >> new ImportResult()
  }

  def "updateImportJob: should update the corresponding import job"() {
    given:
    def importJobEntity = new ImportJobEntity(jobId: UUID.randomUUID())
    def jobState = SUCCESSFUL
    def importResult = new ImportResult(
        totalCountRequested: 10,
        totalCountImported: 5
    )

    when:
    underTest.updateImportJob(importJobEntity, importResult, jobState)

    then:
    1 * underTest.importJobRepository.save({ args ->
      assert args.jobId == importJobEntity.jobId
      assert args.totalCountRequested == importResult.totalCountRequested
      assert args.totalCountImported == importResult.totalCountImported
      assert args.endTime
    })
  }

  def "createImportJob: should call importJobRepository when creating new import job"() {
    given:
    def givenReleaseSource = METAL_ARCHIVES

    when:
    underTest.createImportJob(givenReleaseSource)

    then:
    1 * underTest.importJobRepository.save({ args ->
      assert args.jobId
      assert args.startTime
      assert args.state == RUNNING
      assert args.source == givenReleaseSource
    })
  }

  def "createImportJob: should return created import job"() {
    given:
    def createdJobEntity = new ImportJobEntity(jobId: UUID.randomUUID())
    underTest.importJobRepository.save(*_) >> createdJobEntity

    when:
    def result = underTest.createImportJob()

    then:
    result == createdJobEntity
  }

  def "retryCoverDownload: should call all importers on retryCoverDownload"() {
    given:
    def releaseImporterMock1 = Mock(ReleaseImporter)
    def releaseImporterMock2 = Mock(ReleaseImporter)
    underTest.releaseImporters = [releaseImporterMock1, releaseImporterMock2]

    when:
    underTest.retryCoverDownload()

    then:
    1 * releaseImporterMock1.retryCoverDownload()
    1 * releaseImporterMock2.retryCoverDownload()
  }

  def "retryCoverDownload: should handle any exception"() {
    given:
    underTest.releaseImporters = [Mock(ReleaseImporter)]
    underTest.retryCoverDownload() >> { throw new RuntimeException("boom") }

    when:
    underTest.retryCoverDownload()

    then:
    noExceptionThrown()
  }
}

package rocks.metaldetector.butler.service.importjob

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.metaldetector.butler.config.web.ResourceNotFoundException
import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobEntity
import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobRepository
import rocks.metaldetector.butler.persistence.domain.importjob.JobState
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ImportResult
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ReleaseImporter
import rocks.metaldetector.butler.web.dto.ImportJobDto
import spock.lang.Specification
import spock.lang.Unroll

import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.INITIALIZED
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.RUNNING
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.SUCCESSFUL
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TIME_FOR_METAL
import static rocks.metaldetector.butler.testutil.DtoFactory.ImportJobEntityFactory

class ImportJobServiceTest extends Specification {

  ImportJobService underTest = new ImportJobService(
      importJobRepository: Mock(ImportJobRepository),
      importJobTransformer: Mock(ImportJobTransformer),
      releaseImportTaskExecutor: Mock(ThreadPoolTaskExecutor)
  )

  def "findAllImportJobs: should call import job repository"() {
    when:
    underTest.findAllImportJobs()

    then:
    1 * underTest.importJobRepository.findAll()
  }

  def "findAllImportJobs: should transform each job entity with job transformer"() {
    given:
    def jobEntities = [
        ImportJobEntityFactory.createImportJobEntity(),
        ImportJobEntityFactory.createImportJobEntity()
    ]
    underTest.importJobRepository.findAll() >> jobEntities

    when:
    underTest.findAllImportJobs()

    then:
    1 * underTest.importJobTransformer.transform(jobEntities[0])

    then:
    1 * underTest.importJobTransformer.transform(jobEntities[1])
  }

  def "findAllImportJobs: should return list of transformed import jobs"() {
    given:
    def jobEntities = [
        ImportJobEntityFactory.createImportJobEntity(),
        ImportJobEntityFactory.createImportJobEntity()
    ]
    underTest.importJobRepository.findAll() >> jobEntities

    when:
    def results = underTest.findAllImportJobs()

    then:
    results.size() == jobEntities.size()
  }

  def "findImportJobById: repository is called with given id"() {
    given:
    def jobId = UUID.randomUUID()

    when:
    underTest.findImportJobById(jobId.toString())

    then:
    1 * underTest.importJobRepository.findByJobId(jobId) >> Optional.of(new ImportJobEntity())
  }

  def "findImportJobById: transformer is called with job"() {
    given:
    def jobId = UUID.randomUUID()
    def job = new ImportJobEntity(jobId: jobId)
    underTest.importJobRepository.findByJobId(*_) >> Optional.of(job)

    when:
    underTest.findImportJobById(jobId.toString())

    then:
    1 * underTest.importJobTransformer.transform(job)
  }

  def "findImportJobById: exception is thrown if job does not exist"() {
    given:
    def jobId = UUID.randomUUID().toString()
    underTest.importJobRepository.findByJobId(*_) >> Optional.empty()

    when:
    underTest.findImportJobById(jobId)

    then:
    def thrown = thrown(ResourceNotFoundException)
    thrown.message == "Job $jobId not present"
  }

  def "findImportJobById: dto is returned"() {
    given:
    def jobId = UUID.randomUUID().toString()
    def jobDto = new ImportJobDto(jobId: jobId)
    underTest.importJobRepository.findByJobId(*_) >> Optional.of(new ImportJobEntity())
    underTest.importJobTransformer.transform(*_) >> jobDto

    when:
    def result = underTest.findImportJobById(jobId)

    then:
    result == jobDto
  }

  def "createImportJobs: should create a new import job, schedule a task and return the jobId in this order"() {
    given:
    def releaseImporterMock = Mock(ReleaseImporter)
    underTest.releaseImporters = [releaseImporterMock]
    releaseImporterMock.releaseSource >> METAL_ARCHIVES
    ImportJobEntity metalArchivesImportJob = new ImportJobEntity(jobId: UUID.randomUUID())

    when:
    def result = underTest.createImportJobs()

    then:
    1 * underTest.importJobRepository.save({
      it.jobId != null
      it.state == INITIALIZED
      it.source == METAL_ARCHIVES
    }) >> metalArchivesImportJob

    then:
    1 * underTest.releaseImportTaskExecutor.submit({
      it.releaseImporter == releaseImporterMock
      it.importJobService == underTest
      it.importJob == metalArchivesImportJob
    })

    and:
    result == [metalArchivesImportJob.jobId.toString()]
  }

  def "createImportJobs: should process every release importer"() {
    given:
    def releaseImporterMock1 = Mock(ReleaseImporter)
    def releaseImporterMock2 = Mock(ReleaseImporter)
    underTest.releaseImporters = [releaseImporterMock1, releaseImporterMock2]
    releaseImporterMock1.releaseSource >> METAL_ARCHIVES
    releaseImporterMock2.releaseSource >> TIME_FOR_METAL
    underTest.importJobRepository.save(*_) >> new ImportJobEntity()

    when:
    underTest.createImportJobs()

    then:
    1 * underTest.releaseImportTaskExecutor.submit({ it.releaseImporter == releaseImporterMock1 })

    and:
    1 * underTest.releaseImportTaskExecutor.submit({ it.releaseImporter == releaseImporterMock2 })
  }

  @Unroll
  "updateImportJob: should update the corresponding import job's state to '#jobState'"() {
    given:
    def importJobEntity = new ImportJobEntity(jobId: UUID.randomUUID())

    when:
    underTest.updateImportJob(importJobEntity, jobState)

    then:
    1 * underTest.importJobRepository.save({ args ->
      args.jobId == importJobEntity.jobId
      args.state == jobState
    })

    where:
    jobState << JobState.values()
  }

  def "updateImportJob: if state is 'SUCCESSFUL', import result info is set"() {
    given:
    def importJobEntity = new ImportJobEntity(jobId: UUID.randomUUID())
    def importResult = new ImportResult(
        totalCountRequested: 10,
        totalCountImported: 5
    )

    when:
    underTest.updateImportJob(importJobEntity, SUCCESSFUL, importResult)

    then:
    1 * underTest.importJobRepository.save({ args ->
      args.jobId == importJobEntity.jobId
      args.totalCountRequested == importResult.totalCountRequested
      args.totalCountImported == importResult.totalCountImported
      args.endTime
    })
  }

  def "updateImportJob: if state is 'RUNNING', startTime is set"() {
    given:
    def importJobEntity = new ImportJobEntity(jobId: UUID.randomUUID())

    when:
    underTest.updateImportJob(importJobEntity, RUNNING)

    then:
    1 * underTest.importJobRepository.save({ args ->
      args.jobId == importJobEntity.jobId
      args.startTime
    })
  }

  def "createImportJob: should call importJobRepository when creating new import job"() {
    given:
    def givenReleaseSource = METAL_ARCHIVES

    when:
    underTest.createImportJob(givenReleaseSource)

    then:
    1 * underTest.importJobRepository.save({ args ->
      args.jobId
      args.state == INITIALIZED
      args.source == givenReleaseSource
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

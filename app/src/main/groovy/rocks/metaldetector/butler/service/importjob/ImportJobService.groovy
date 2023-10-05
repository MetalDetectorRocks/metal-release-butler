package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobEntity
import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobRepository
import rocks.metaldetector.butler.persistence.domain.importjob.JobState
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ImportResult
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ReleaseImporter
import rocks.metaldetector.butler.web.dto.ImportJobDto

import java.time.LocalDateTime

import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.INITIALIZED
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.RUNNING
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.SUCCESSFUL

@Service
@Slf4j
class ImportJobService {

  @Autowired
  ImportJobRepository importJobRepository

  @Autowired
  ImportJobTransformer importJobTransformer

  @Autowired
  List<ReleaseImporter> releaseImporters

  @Autowired
  ThreadPoolTaskExecutor releaseImportTaskExecutor

  @Scheduled(cron = "0 0 2 * * *")
  List<String> createImportJobs() {
    log.info("Start import of new releases...")
    return releaseImporters.collect { releaseImporter ->
      ImportJobEntity job = createImportJob(releaseImporter.releaseSource)
      releaseImportTaskExecutor.submit(new ImportJobTask(releaseImporter: releaseImporter,
                                                         importJobService: this,
                                                         importJob: job))
      return job.jobId.toString()
    }
  }

  @Async
  @Scheduled(cron = "0 0 3 * * *")
  void retryCoverDownload() {
    log.info("Start cover download retry...")
    releaseImporters.each { releaseImporter ->
      try {
        releaseImporter.retryCoverDownload()
      }
      catch (Exception e) {
        log.error("Error during cover download from '${releaseImporter.getReleaseSource().displayName}'", e)
      }
    }
  }

  @Transactional(readOnly = true)
  List<ImportJobDto> findAllImportJobs() {
    return importJobRepository.findAll().collect {
      importJobTransformer.transform(it)
    }
  }

  @Transactional(readOnly = true)
  ImportJobDto findImportJobById(String jobId) {
    ImportJobEntity importJob = importJobRepository.findByJobId(UUID.fromString(jobId))
    return importJobTransformer.transform(importJob)
  }

  @Transactional
  void updateImportJob(ImportJobEntity importJobEntity, JobState jobState, ImportResult importResult = null) {
    if (jobState == SUCCESSFUL) {
      importJobEntity.totalCountRequested = importResult?.totalCountRequested
      importJobEntity.totalCountImported = importResult?.totalCountImported
      importJobEntity.endTime = LocalDateTime.now()
    }
    else if (jobState == RUNNING) {
      importJobEntity.startTime = LocalDateTime.now()
    }

    importJobEntity.state = jobState
    importJobRepository.save(importJobEntity)
  }

  @Transactional
  ImportJobEntity createImportJob(ReleaseSource source) {
    ImportJobEntity importJobEntity = new ImportJobEntity(
        jobId: UUID.randomUUID(),
        state: INITIALIZED,
        source: source
    )
    importJobEntity = importJobRepository.save(importJobEntity)
    return importJobEntity
  }
}

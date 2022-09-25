package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
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

import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.ERROR
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

  @Async
  @Scheduled(cron = "0 0 2 * * *")
  void importFromExternalSources() {
    releaseImporters.each { releaseImporter ->
      ImportJobEntity job = createImportJob(releaseImporter.releaseSource)
      try {
        ImportResult importResult = releaseImporter.importReleases()
        updateImportJob(job, importResult, SUCCESSFUL)
      }
      catch (Exception e) {
        log.error("Error during import of releases from '${releaseImporter.getReleaseSource().displayName}'", e)
        updateImportJob(job, new ImportResult(), ERROR)
      }
    }
  }

  @Async
  void retryCoverDownload() {
    releaseImporters.each { releaseImporter ->
      try {
        releaseImporter.retryCoverDownload()
      }
      catch (Exception e) {
        log.error("Error during cover download from '${releaseImporter.getReleaseSource().displayName}'", e)
      }
    }
  }

  @Transactional
  List<ImportJobDto> findAllImportJobResults() {
    return importJobRepository.findAll().collect {
      importJobTransformer.transform(it)
    }
  }

  @Transactional
  void updateImportJob(ImportJobEntity importJobEntity, ImportResult importResult, JobState jobState) {
    importJobEntity.totalCountRequested = importResult.totalCountRequested
    importJobEntity.totalCountImported = importResult.totalCountImported
    importJobEntity.state = jobState
    importJobEntity.endTime = LocalDateTime.now()
    importJobRepository.save(importJobEntity)
  }

  @Transactional
  ImportJobEntity createImportJob(ReleaseSource source) {
    ImportJobEntity importJobEntity = new ImportJobEntity(
        jobId: UUID.randomUUID(),
        startTime: LocalDateTime.now(),
        state: RUNNING,
        source: source
    )
    importJobEntity = importJobRepository.save(importJobEntity)
    return importJobEntity
  }
}

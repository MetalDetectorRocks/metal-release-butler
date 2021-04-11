package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.importjob.JobState
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.web.dto.ImportJobDto

import javax.annotation.PostConstruct
import java.time.LocalDateTime

import static rocks.metaldetector.butler.model.importjob.JobState.ERROR
import static rocks.metaldetector.butler.model.importjob.JobState.RUNNING
import static rocks.metaldetector.butler.model.importjob.JobState.SUCCESSFUL

@Service
@Slf4j
class ImportJobService {

  @Autowired
  ImportJobRepository importJobRepository

  @Autowired
  ImportJobTransformer importJobTransformer

  @Autowired
  ReleaseImporter metalArchivesReleaseImporter

  @Autowired
  ReleaseImporter timeForMetalReleaseImporter

  List<ReleaseImporter> releaseImporters

  @PostConstruct
  private void init() {
    releaseImporters = [
        //metalArchivesReleaseImporter,
        timeForMetalReleaseImporter
    ]
  }

  @Async
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

  @Transactional(readOnly = false)
  void updateImportJob(ImportJobEntity importJobEntity, ImportResult importResult, JobState jobState) {
    importJobEntity.totalCountRequested = importResult.totalCountRequested
    importJobEntity.totalCountImported = importResult.totalCountImported
    importJobEntity.state = jobState
    importJobEntity.endTime = LocalDateTime.now()
    importJobRepository.save(importJobEntity)
  }

  @Transactional(readOnly = false)
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

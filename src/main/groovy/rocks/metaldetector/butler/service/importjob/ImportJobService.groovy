package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.web.dto.ImportJobResponse

import javax.annotation.PostConstruct
import java.time.LocalDateTime

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
  ReleaseImporter metalHammerReleaseImporter

  List<ReleaseImporter> releaseImporters

  @PostConstruct
  private void init() {
    releaseImporters = [
            metalArchivesReleaseImporter,
            metalHammerReleaseImporter
    ]
  }

  @Async
  void importFromExternalSources() {
    releaseImporters.each { releaseImporter ->
      ImportJobEntity job = createImportJob(releaseImporter.releaseSource)
      ImportResult importResult = releaseImporter.importReleases()
      updateImportJob(job, importResult)
    }
  }

  @Transactional
  List<ImportJobResponse> findAllImportJobResults() {
    return importJobRepository.findAll().collect {
      importJobTransformer.transform(it)
    }
  }

  @Transactional(readOnly = false)
  void updateImportJob(ImportJobEntity importJobEntity, ImportResult importResult) {
    importJobEntity.totalCountRequested = importResult.totalCountRequested
    importJobEntity.totalCountImported = importResult.totalCountImported
    importJobEntity.endTime = LocalDateTime.now()
    importJobRepository.save(importJobEntity)
  }

  @Transactional(readOnly = false)
  ImportJobEntity createImportJob(ReleaseSource source) {
    ImportJobEntity importJobEntity = new ImportJobEntity(
            jobId: UUID.randomUUID(),
            startTime: LocalDateTime.now(),
            source: source
    )
    importJobEntity = importJobRepository.save(importJobEntity)
    return importJobEntity
  }
}

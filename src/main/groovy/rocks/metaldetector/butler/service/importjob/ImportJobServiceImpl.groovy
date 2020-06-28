package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.web.dto.CreateImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobResponse

import java.time.LocalDateTime

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE

@Service
@Slf4j
class ImportJobServiceImpl implements ImportJobService {

  @Autowired
  ImportJobRepository importJobRepository

  @Autowired
  ImportJobTransformer importJobTransformer

  @Autowired
  MetalArchivesReleaseImporter metalArchivesReleaseImportService

  @Autowired
  MetalHammerReleaseImporter metalHammerReleaseImportService

  @Override
  @Transactional
  CreateImportJobResponse importFromExternalSources() {
    ImportJobEntity metalArchivesImportJob = createImportJob(METAL_ARCHIVES)
    metalArchivesReleaseImportService.importReleases(metalArchivesImportJob.id)
    ImportJobEntity metalHammerImportJob = createImportJob(METAL_HAMMER_DE)
    metalHammerReleaseImportService.importReleases(metalHammerImportJob.id)
    CreateImportJobResponse response = new CreateImportJobResponse(jobIds: [metalArchivesImportJob.jobId, metalHammerImportJob.jobId])
    return response
  }

  @Override
  List<ImportJobResponse> findAllImportJobResults() {
    return importJobRepository.findAll().collect {
      importJobTransformer.transform(it)
    }
  }

  private ImportJobEntity createImportJob(ReleaseSource source) {
    ImportJobEntity importJobEntity = new ImportJobEntity(
            jobId: UUID.randomUUID(),
            startTime: LocalDateTime.now(),
            source: source
    )
    importJobEntity = importJobRepository.save(importJobEntity)
    return importJobEntity
  }
}

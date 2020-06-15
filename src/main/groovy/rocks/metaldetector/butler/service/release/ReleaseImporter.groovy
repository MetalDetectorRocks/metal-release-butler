package rocks.metaldetector.butler.service.release

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.transformer.ImportJobTransformer
import rocks.metaldetector.butler.web.dto.ImportJobResponse

import java.time.LocalDateTime

abstract class ReleaseImporter {

  @Autowired
  ImportJobRepository importJobRepository

  @Autowired
  ReleaseRepository releaseRepository

  @Autowired
  ImportJobTransformer importJobTransformer

  @Async
  abstract ImportJobResponse importReleases(Long internalJobId)

  ImportJobEntity updateImportJob(Long internalJobId, int totalCountRequested, int totalCountImported) {
    ImportJobEntity importJobEntity = importJobRepository.findById(internalJobId).get()
    importJobEntity.totalCountRequested = totalCountRequested
    importJobEntity.totalCountImported = totalCountImported
    importJobEntity.endTime = LocalDateTime.now()

    return importJobRepository.save(importJobEntity)
  }
}

package rocks.metaldetector.butler.service.importjob

import org.springframework.stereotype.Service
import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobEntity
import rocks.metaldetector.butler.web.dto.ImportJobDto

@Service
class ImportJobTransformer {

  ImportJobDto transform(ImportJobEntity importJobEntity) {
    if (importJobEntity) {
      return new ImportJobDto(
              jobId: importJobEntity.jobId,
              totalCountRequested: importJobEntity.totalCountRequested,
              totalCountImported: importJobEntity.totalCountImported,
              startTime: importJobEntity.startTime,
              endTime: importJobEntity.endTime,
              state: importJobEntity.state?.displayName,
              source: importJobEntity.source?.displayName
      )
    }
  }
}

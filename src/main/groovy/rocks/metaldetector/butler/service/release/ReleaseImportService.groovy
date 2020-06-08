package rocks.metaldetector.butler.service.release

import org.springframework.scheduling.annotation.Async
import rocks.metaldetector.butler.web.dto.ImportJobResponse

interface ReleaseImportService {

  @Async
  ImportJobResponse importReleases(Long internalJobId)

}

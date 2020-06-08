package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.web.dto.ImportJobResponse

interface ReleaseImportService {

  ImportJobResponse importReleases(Long internalJobId)

}

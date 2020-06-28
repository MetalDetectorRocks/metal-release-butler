package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.web.dto.CreateImportJobResponse
import rocks.metaldetector.butler.web.dto.ImportJobResponse

interface ImportJobService {

  CreateImportJobResponse importFromExternalSources()

  List<ImportJobResponse> findAllImportJobResults()

}
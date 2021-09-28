package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical
import rocks.metaldetector.butler.web.dto.ImportJobDto

@Canonical
class ImportJobResponse {

  List<ImportJobDto> importJobs
}

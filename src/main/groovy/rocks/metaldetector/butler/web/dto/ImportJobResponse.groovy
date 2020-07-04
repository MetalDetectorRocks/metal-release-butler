package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical

@Canonical
class ImportJobResponse {

  List<ImportJobDto> importJobs
}

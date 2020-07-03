package rocks.metaldetector.butler.service.importjob

import groovy.transform.Canonical

@Canonical
class ImportResult {

  int totalCountRequested
  int totalCountImported

}

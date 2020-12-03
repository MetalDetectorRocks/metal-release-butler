package rocks.metaldetector.butler.service.importjob

import groovy.transform.Canonical

@Canonical
class ImportResult {

  Integer totalCountRequested
  Integer totalCountImported

}

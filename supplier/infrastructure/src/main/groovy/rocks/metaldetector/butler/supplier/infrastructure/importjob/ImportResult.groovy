package rocks.metaldetector.butler.supplier.infrastructure.importjob

import groovy.transform.Canonical

@Canonical
class ImportResult {

  Integer totalCountRequested
  Integer totalCountImported

}

package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical

@Canonical
class ReleaseImportResponse {

  int totalCountRequested
  int totalCountImported

}

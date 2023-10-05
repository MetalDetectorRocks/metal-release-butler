package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical

@Canonical
class ImportJobCreatedResponse {

  List<String> importJobIds
}

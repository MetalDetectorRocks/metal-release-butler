package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical

@Canonical
class CreateImportJobResponse {

  List<UUID> jobIds
}
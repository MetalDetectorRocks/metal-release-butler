package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical
import rocks.metaldetector.butler.web.dto.ReleaseDto

@Canonical
class ReleasesResponse {

  Pagination pagination
  Iterable<ReleaseDto> releases

}

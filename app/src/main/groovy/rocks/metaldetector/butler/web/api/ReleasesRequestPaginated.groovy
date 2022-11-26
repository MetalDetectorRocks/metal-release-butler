package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@Canonical
class ReleasesRequestPaginated extends ReleasesRequest {

  @Min(value = 1L, message = "'page' must be greater than zero!")
  int page

  @Min(value = 1L, message = "'size' must be greater than zero!")
  @Max(value = 50L, message = "'size' must be equal or less than 50!")
  int size

  String query

}

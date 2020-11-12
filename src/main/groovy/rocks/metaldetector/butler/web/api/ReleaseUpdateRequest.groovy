package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical
import rocks.metaldetector.butler.model.release.ReleaseEntityState

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@Canonical
class ReleaseUpdateRequest {

  @Min(value = 1L, message = "'releaseId' must be greater than zero!")
  long releaseId

  @NotNull
  ReleaseEntityState state

}

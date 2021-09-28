package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState

import javax.validation.constraints.NotNull

@Canonical
class ReleaseUpdateRequest {

  @NotNull
  ReleaseEntityState state

}

package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical
import jakarta.validation.constraints.NotNull
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState

@Canonical
class ReleaseUpdateRequest {

  @NotNull
  ReleaseEntityState state

}

package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical

@Canonical
class ReleasesResponse {

  int currentPage
  int size
  int totalPages
  long totalReleases
  Iterable<ReleaseDto> releases

}

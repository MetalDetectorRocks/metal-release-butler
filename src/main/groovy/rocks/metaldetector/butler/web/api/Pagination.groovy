package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical

@Canonical
class Pagination {

  int currentPage
  int size
  int totalPages
  long totalReleases

}

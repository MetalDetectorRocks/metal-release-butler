package com.metalr2.butler.web.dto

import groovy.transform.Canonical

@Canonical
class UpcomingReleasesResponse {

  int currentPage
  int size
  int totalPages
  long totalReleases
  List<ReleaseDto> releases

}

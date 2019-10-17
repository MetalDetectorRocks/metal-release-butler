package com.metalr2.butler.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Canonical

@Canonical
class UpcomingReleasesResponseDto {

  @JsonProperty("iTotalRecords")
  long totalRecords

  @JsonProperty("aaData")
  List<String[]> data

}

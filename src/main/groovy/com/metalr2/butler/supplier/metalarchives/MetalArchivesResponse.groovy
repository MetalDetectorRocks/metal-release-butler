package com.metalr2.butler.supplier.metalarchives

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Canonical

@Canonical
class MetalArchivesResponse {

  @JsonProperty("iTotalRecords")
  long totalRecords

  @JsonProperty("aaData")
  List<String[]> data

}

package rocks.metaldetector.butler.supplier.metalarchives

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Canonical

@Canonical
class MetalArchivesReleasesResponse {

  @JsonProperty("iTotalRecords")
  long totalRecords

  @JsonProperty("aaData")
  List<String[]> data

}

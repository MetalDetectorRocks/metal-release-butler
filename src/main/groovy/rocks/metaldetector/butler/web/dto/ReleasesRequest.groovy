package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical
import org.springframework.format.annotation.DateTimeFormat

import java.time.LocalDate

@Canonical
class ReleasesRequest {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate dateFrom

}
// ToDo DanielW: Cleanup object instantiation in tests etc.
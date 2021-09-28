package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical
import org.springframework.format.annotation.DateTimeFormat

import java.time.LocalDate

@Canonical
class ReleasesRequest {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate dateFrom

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate dateTo

  Iterable<String> artists

  Iterable<String> getArtists() {
    return artists ?: Collections.emptyList() as Iterable<String>
  }

}

package rocks.metaldetector.butler.web.api

import groovy.transform.Canonical
import jakarta.validation.constraints.AssertTrue
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

  @AssertTrue(message = "If both dates are set, dateFrom has to be equal to or before dateTo!")
  boolean isDateFromBeforeDateTo() {
    if (dateFrom != null && dateTo != null) {
      return dateFrom == dateTo || dateFrom.isBefore(dateTo)
    }
    return true
  }

  @AssertTrue(message = "dateTo cannot be set without dateFrom!")
  boolean isValidRange() {
    return dateTo == null || dateFrom != null
  }
}

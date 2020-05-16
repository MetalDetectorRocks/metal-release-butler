package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical
import org.springframework.format.annotation.DateTimeFormat

import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import java.time.LocalDate

@Canonical
class ReleasesRequestPaginated {

  @Min(value = 1L, message = "'page' must be greater than zero!")
  int page

  @Min(value = 1L, message = "'size' must be greater than zero!")
  @Max(value = 50L, message = "'size' must be equal or less than 50!")
  int size

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate dateFrom

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  LocalDate dateTo

  Iterable<String> artists

  Iterable<String> getArtists() {
    return artists ?: Collections.emptyList() as Iterable<String>
  }

  @AssertTrue(message = "If dates are set, dateFrom has to be equal to or before dateTo!")
  private boolean isValid() {
    if (dateFrom != null && dateTo != null) {
      return dateFrom == dateTo || dateFrom.isBefore(dateTo)
    }
    return true
  }
}

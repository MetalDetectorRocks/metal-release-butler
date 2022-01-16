package rocks.metaldetector.butler.web.api

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class ReleasesRequestTest extends Specification {

  @Unroll
  "If both dates are set, dateFrom has to be equal to or before dateTo"() {
    given:
    def request = new ReleasesRequest(dateFrom: dateFrom,
                                      dateTo: dateTo)

    when:
    var result = request.isValidIfSetFromBeforeTo()

    then:
    result == expectedResult

    where:
    dateFrom                 | dateTo                   | expectedResult
    LocalDate.of(2020, 1, 1) | LocalDate.of(2020, 1, 1) | true
    LocalDate.of(2020, 1, 1) | LocalDate.of(2021, 1, 1) | true
    LocalDate.of(2021, 1, 1) | LocalDate.of(2020, 1, 1) | false
    null                     | null                     | true
  }

  @Unroll
  "dateTo cannot be set without dateFrom"() {
    given:
    def request = new ReleasesRequest(dateFrom: dateFrom,
                                      dateTo: dateTo)

    when:
    var result = request.isValidNotOnlyDateToSet()

    then:
    result == expectedResult

    where:
    dateFrom                 | dateTo                   | expectedResult
    null                     | LocalDate.of(2020, 1, 1) | false
    LocalDate.of(2020, 1, 1) | LocalDate.of(2021, 1, 1) | true
    LocalDate.of(2020, 1, 1) | null                     | true
    null                     | null                     | true
  }
}

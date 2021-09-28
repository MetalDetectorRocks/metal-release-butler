package rocks.metaldetector.butler.service.release

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification
import spock.lang.Unroll

class PageTransformerTest extends Specification {

  PageTransformer underTest = new PageTransformer()

  @Unroll
  "should transform page to pagination"() {
    given:
    def pageObj = new PageImpl([], PageRequest.of(givenPage, size), total)

    when:
    def result = underTest.transform(pageObj)

    then:
    result.currentPage == expectedPage
    result.size == size
    result.totalPages == totalPages
    result.totalReleases == total

    where:
    givenPage | expectedPage | size | total | totalPages
    0         | 1            | 10   | 99    | 10
    0         | 1            | 10   | 100   | 10
    0         | 1            | 10   | 101   | 11
    1         | 2            | 10   | 100   | 10
  }
}

package rocks.metaldetector.butler.service.statistics

import rocks.metaldetector.butler.persistence.domain.release.ReleasePerMonth
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import spock.lang.Specification

import java.time.LocalDate
import java.time.YearMonth

import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.DEMO
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.DUPLICATE

class StatisticsServiceTest extends Specification {

  StatisticsService underTest = new StatisticsService(releaseRepository: Mock(ReleaseRepository))

  def "releaseRepository is called for grouped releases"() {
    given:
    underTest.releaseRepository.countByState(*_) >> 0
    underTest.releaseRepository.countByReleaseDateAfterAndStateNot(*_) >> 0

    when:
    underTest.getReleaseInfo()

    then:
    1 * underTest.releaseRepository.groupReleasesByYearAndMonth() >> []
  }

  def "releaseRepository is called to count duplicates"() {
    given:
    underTest.releaseRepository.countByReleaseDateAfterAndStateNot(*_) >> 0
    underTest.releaseRepository.groupReleasesByYearAndMonth() >> []

    when:
    underTest.getReleaseInfo()

    then:
    1 * underTest.releaseRepository.countByState(DUPLICATE) >> 0
  }

  def "releaseRepository is called to count upcoming releases"() {
    given:
    underTest.releaseRepository.countByState(*_) >> 0
    underTest.releaseRepository.groupReleasesByYearAndMonth() >> []

    GroovyMock(LocalDate, global: true)
    def releaseDate = LocalDate.of(2020, 1, 1)
    LocalDate.now() >> releaseDate

    when:
    underTest.getReleaseInfo()

    then:
    1 * underTest.releaseRepository.countByReleaseDateAfterAndStateNot(releaseDate, DEMO) >> 0
  }

  def "releasesPerMonth are returned"() {
    given:
    underTest.releaseRepository.countByState(*_) >> 0
    underTest.releaseRepository.countByReleaseDateAfterAndStateNot(*_) >> 0

    def releasePerMonth1 = Mock(ReleasePerMonth)
    def releasePerMonth2 = Mock(ReleasePerMonth)
    releasePerMonth1.releaseYear >> 2020
    releasePerMonth2.releaseYear >> 2020
    releasePerMonth1.releaseMonth >> 1
    releasePerMonth2.releaseMonth >> 2
    releasePerMonth1.releases >> 3
    releasePerMonth2.releases >> 6
    underTest.releaseRepository.groupReleasesByYearAndMonth() >> [releasePerMonth1, releasePerMonth2]

    when:
    def result = underTest.getReleaseInfo()

    then:
    result.releasesPerMonth == [(YearMonth.of(2020, 1)): 3,
                                (YearMonth.of(2020, 2)): 6]
  }

  def "totalReleases are returned"() {
    given:
    underTest.releaseRepository.countByState(*_) >> 0
    underTest.releaseRepository.countByReleaseDateAfterAndStateNot(*_) >> 0

    def releasePerMonth1 = Mock(ReleasePerMonth)
    def releasePerMonth2 = Mock(ReleasePerMonth)
    releasePerMonth1.releaseYear >> 2020
    releasePerMonth2.releaseYear >> 2020
    releasePerMonth1.releaseMonth >> 1
    releasePerMonth2.releaseMonth >> 2
    releasePerMonth1.releases >> 3
    releasePerMonth2.releases >> 6
    underTest.releaseRepository.groupReleasesByYearAndMonth() >> [releasePerMonth1, releasePerMonth2]

    when:
    def result = underTest.getReleaseInfo()

    then:
    result.totalReleases == 9
  }

  def "upcomingReleases are returned"() {
    given:
    underTest.releaseRepository.countByState(*_) >> 0
    underTest.releaseRepository.groupReleasesByYearAndMonth() >> []
    underTest.releaseRepository.countByReleaseDateAfterAndStateNot(*_) >> 6

    when:
    def result = underTest.getReleaseInfo()

    then:
    result.upcomingReleases == 6
  }

  def "releasesThisMonth are returned"() {
    given:
    underTest.releaseRepository.countByState(*_) >> 0
    underTest.releaseRepository.countByReleaseDateAfterAndStateNot(*_) >> 0

    def thisMonth = LocalDate.now()
    def releasePerMonth = Mock(ReleasePerMonth)
    releasePerMonth.releaseYear >> thisMonth.year
    releasePerMonth.releaseMonth >> thisMonth.month.value
    releasePerMonth.releases >> 6
    underTest.releaseRepository.groupReleasesByYearAndMonth() >> [releasePerMonth]

    when:
    def result = underTest.getReleaseInfo()

    then:
    result.releasesThisMonth == 6
  }

  def "duplicates are returned"() {
    given:
    underTest.releaseRepository.countByState(*_) >> 6
    underTest.releaseRepository.countByReleaseDateAfterAndStateNot(*_) >> 0
    underTest.releaseRepository.groupReleasesByYearAndMonth() >> []

    when:
    def result = underTest.getReleaseInfo()

    then:
    result.duplicates == 6
  }
}

package rocks.metaldetector.butler.service.statistics

import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobRepository
import rocks.metaldetector.butler.persistence.domain.release.ReleasePerMonth
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.SUCCESSFUL
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.DEMO
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.DUPLICATE
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TIME_FOR_METAL

class StatisticsServiceTest extends Specification {

  StatisticsService underTest = new StatisticsService(releaseRepository: Mock(ReleaseRepository),
                                                      importJobRepository: Mock(ImportJobRepository))

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

  def "successRate is calculated for all sources"() {
    when:
    def result = underTest.getImportInfo()

    then:
    1 * underTest.importJobRepository.countBySource(METAL_ARCHIVES) >> 10
    1 * underTest.importJobRepository.countBySourceAndState(METAL_ARCHIVES, SUCCESSFUL) >> 9
    result.find { it.source == METAL_ARCHIVES.name() }.successRate == 90

    and:
    1 * underTest.importJobRepository.countBySource(TIME_FOR_METAL) >> 10
    1 * underTest.importJobRepository.countBySourceAndState(TIME_FOR_METAL, SUCCESSFUL) >> 5
    result.find { it.source == TIME_FOR_METAL.name() }.successRate == 50
  }

  def "lastImport is fetched for all sources"() {
    given:
    underTest.importJobRepository.countBySource(*_) >> 10
    underTest.importJobRepository.countBySourceAndState(*_) >> 10
    def expectedTime1 = LocalDateTime.of(2020, 1, 1, 1, 1)
    def expectedTime2 = LocalDateTime.of(2020, 1, 2, 1, 1)

    when:
    def result = underTest.getImportInfo()

    then:
    1 * underTest.importJobRepository.findLastStartTime(METAL_ARCHIVES) >> expectedTime1
    1 * underTest.importJobRepository.findLastStartTime(TIME_FOR_METAL) >> expectedTime2

    and:
    result.find { it.source == METAL_ARCHIVES.name() }.lastImport == expectedTime1
    result.find { it.source == TIME_FOR_METAL.name() }.lastImport == expectedTime2
  }

  def "lastSuccessfulImport is fetched for all sources"() {
    given:
    underTest.importJobRepository.countBySource(*_) >> 10
    underTest.importJobRepository.countBySourceAndState(*_) >> 10
    def expectedTime1 = LocalDateTime.of(2020, 1, 1, 1, 1)
    def expectedTime2 = LocalDateTime.of(2020, 1, 2, 1, 1)

    when:
    def result = underTest.getImportInfo()

    then:
    1 * underTest.importJobRepository.findLastStartTimeByState(METAL_ARCHIVES, SUCCESSFUL) >> expectedTime1
    1 * underTest.importJobRepository.findLastStartTimeByState(TIME_FOR_METAL, SUCCESSFUL) >> expectedTime2

    and:
    result.find { it.source == METAL_ARCHIVES.name() }.lastSuccessfulImport == expectedTime1
    result.find { it.source == TIME_FOR_METAL.name() }.lastSuccessfulImport == expectedTime2
  }
}

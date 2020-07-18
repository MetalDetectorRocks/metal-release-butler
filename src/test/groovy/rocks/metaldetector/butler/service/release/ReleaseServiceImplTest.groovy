package rocks.metaldetector.butler.service.release

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.model.release.ReleaseRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory

class ReleaseServiceImplTest extends Specification {

  ReleaseServiceImpl underTest = new ReleaseServiceImpl(
      releaseRepository: Mock(ReleaseRepository),
      releaseTransformer: Mock(ReleaseTransformer)
  )

  static final LocalDate NOW = LocalDate.now()

  def "findAllUpcomingReleases paginated: if artistNames is empty all releases are requested from releaseRepository"() {
    given:
    def page = 1
    def size = 10
    def expectedPageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle"))

    when:
    underTest.findAllUpcomingReleases([], page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(NOW - 1, expectedPageRequest) >> Page.empty()
  }

  def "findAllUpcomingReleases paginated: if artistNames is empty releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ]
    underTest.releaseRepository.findAllByReleaseDateAfter(*_) >> new PageImpl(releaseEntities)

    when:
    underTest.findAllUpcomingReleases([], 1, 10)

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  def "findAllUpcomingReleases paginated: if artistNames is not empty all given artist's releases are requested from releaseRepository"() {
    given:
    def page = 1
    def size = 10
    def artistNames = ["A1"]
    def expectedPageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle"))

    when:
    underTest.findAllUpcomingReleases(artistNames, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(NOW - 1, artistNames, expectedPageRequest) >> Page.empty()
  }

  def "findAllUpcomingReleases paginated: if artistNames is not empty releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)
    ]
    underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(*_) >> new PageImpl(releaseEntities)

    when:
    underTest.findAllUpcomingReleases(["A1"], 1, 10)

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  def "findAllReleasesForTimeRange paginated: if artistNames is empty all releases are requested from releaseRepository"() {
    given:
    def page = 1
    def size = 10
    def timerange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())
    def expectedPageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle"))

    when:
    underTest.findAllReleasesForTimeRange([], timerange, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateBetween(timerange.from, timerange.to, expectedPageRequest) >> Page.empty()
  }

  def "findAllReleasesForTimeRange paginated: if artistNames is empty releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ]
    underTest.releaseRepository.findAllByReleaseDateBetween(*_) >> new PageImpl(releaseEntities)

    when:
    underTest.findAllReleasesForTimeRange([], TimeRange.of(LocalDate.now() - 1, LocalDate.now()), 1, 10)

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  def "findAllReleasesForTimeRange paginated: if artistNames is not empty all given artist's releases are requested from releaseRepository"() {
    given:
    def page = 1
    def size = 10
    def artistNames = ["A1"]
    def timerange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())
    def expectedPageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle"))

    when:
    underTest.findAllReleasesForTimeRange(artistNames, timerange, page, size)

    then:
    1 * underTest.releaseRepository.findAllByArtistInAndReleaseDateBetween(artistNames, timerange.from, timerange.to, expectedPageRequest) >> Page.empty()
  }

  def "findAllReleasesForTimeRange paginated: if artistNames is not empty releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)
    ]
    underTest.releaseRepository.findAllByArtistInAndReleaseDateBetween(*_) >> new PageImpl(releaseEntities)

    when:
    underTest.findAllReleasesForTimeRange(["A1"], TimeRange.of(LocalDate.now() - 1, LocalDate.now()), 1, 10)

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  def "findAllUpcomingReleases: if artistNames is empty all releases are requested from releaseRepository"() {
    when:
    underTest.findAllUpcomingReleases([])

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(NOW - 1) >> []
  }

  def "findAllUpcomingReleases: if artistNames is empty releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A2", NOW)]
    underTest.releaseRepository.findAllByReleaseDateAfter(*_) >> releaseEntities

    when:
    underTest.findAllUpcomingReleases([])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  def "findAllUpcomingReleases: if artistNames is not empty all given artist's releases are requested from releaseRepository"() {
    given:
    def artistNames = ["A1"]

    when:
    underTest.findAllUpcomingReleases(artistNames)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(NOW - 1, artistNames) >> []
  }

  def "findAllUpcomingReleases: if artistNames is not empty releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(*_) >> releaseEntities

    when:
    underTest.findAllUpcomingReleases(["A1"])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  def "findAllReleasesForTimeRange: if artistNames is empty all releases are requested from releaseRepository"() {
    given:
    def timerange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())

    when:
    underTest.findAllReleasesForTimeRange([], timerange)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateBetween(timerange.from, timerange.to) >> []
  }

  def "findAllReleasesForTimeRange: if artistNames is empty releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A2", NOW)]
    underTest.releaseRepository.findAllByReleaseDateBetween(*_) >> releaseEntities

    when:
    underTest.findAllReleasesForTimeRange([], TimeRange.of(LocalDate.now() - 1, LocalDate.now()))

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  def "findAllReleasesForTimeRange: if artistNames is not empty all given artist's releases are requested from releaseRepository"() {
    given:
    def artistNames = ["A1"]
    def timerange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())

    when:
    underTest.findAllReleasesForTimeRange(artistNames, timerange)

    then:
    1 * underTest.releaseRepository.findAllByArtistInAndReleaseDateBetween(artistNames, timerange.from, timerange.to) >> []
  }

  def "findAllReleasesForTimeRange: if artistNames is not empty releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByArtistInAndReleaseDateBetween(*_) >> releaseEntities

    when:
    underTest.findAllReleasesForTimeRange(["A1"], TimeRange.of(LocalDate.now() - 1, LocalDate.now()))

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  @Unroll
  "findAllReleasesSince: releaseRepository is called with a correct date (day: #day)"() {
    when:
    underTest.findAllReleasesSince(day)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(LocalDate.now().minusDays(day))

    where:
    day << [-10, 0, 10]
  }

  def "findAllReleasesSince: releaseTransformer is called for every entity"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByReleaseDateAfter(*_) >> releaseEntities

    when:
    underTest.findAllReleasesSince(1)

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[0])

    then:
    1 * underTest.releaseTransformer.transform(releaseEntities[1])
  }

  @Unroll
  "count all upcoming releases for #artists"() {
    when:
    long result = underTest.totalCountAllUpcomingReleases(artists)

    then:
    1 * underTest.releaseRepository.countByArtistInAndReleaseDateAfter(artists, LocalDate.now() - 1) >> expectedNumber

    and:
    result == expectedNumber

    where:
    artists            | expectedNumber
    ["A0", "A1", "A2"] | 3
    ["A0", "A1"]       | 2
  }

  def "count all upcoming releases"() {
    when:
    long result = underTest.totalCountAllUpcomingReleases([])

    then:
    1 * underTest.releaseRepository.countByReleaseDateAfter(LocalDate.now() - 1) >> 1

    and:
    result == 1
  }

  def "count all upcoming releases for artists and timeRange calls releaseRepository"() {
    given:
    def artists = ["A1", "A2"]
    def timeRange = TimeRange.of(LocalDate.of(2020, 1, 1),
                                 LocalDate.of(2020, 2, 28))

    when:
    underTest.totalCountAllReleasesForTimeRange(artists, timeRange)

    then:
    1 * underTest.releaseRepository.countByArtistInAndReleaseDateBetween(artists, timeRange.from, timeRange.to)
  }

  def "count all upcoming releases for artists and timeRange return given value"() {
    given:
    def expectedNumber = 5
    underTest.releaseRepository.countByArtistInAndReleaseDateBetween(*_) >> expectedNumber

    when:
    long result = underTest.totalCountAllReleasesForTimeRange(["A1"], TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1)))

    then:
    result == expectedNumber
  }

  def "count all upcoming releases in timeRange calls releaseRepository"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)

    when:
    underTest.totalCountAllReleasesForTimeRange([], TimeRange.of(from, to))

    then:
    1 * underTest.releaseRepository.countByReleaseDateBetween(from, to)
  }

  def "count all upcoming releases in timeRange returns given value"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)
    def expectedNumber = 5
    underTest.releaseRepository.countByReleaseDateBetween(*_) >> expectedNumber

    when:
    long result = underTest.totalCountAllReleasesForTimeRange([], TimeRange.of(from, to))

    then:
    result == expectedNumber
  }
}

package rocks.metaldetector.butler.service.release

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import rocks.metaldetector.butler.config.web.ResourceNotFoundException
import rocks.metaldetector.butler.persistence.domain.TimeRange
import rocks.metaldetector.butler.persistence.domain.release.ReleaseRepository
import rocks.metaldetector.butler.web.api.ReleasesResponse
import spock.lang.Specification

import java.time.LocalDate

import static org.springframework.data.domain.Sort.Direction.DESC
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.FAULTY
import static rocks.metaldetector.butler.testutil.DtoFactory.ReleaseEntityFactory

class ReleaseServiceImplTest extends Specification {

  ReleaseServiceImpl underTest = new ReleaseServiceImpl(
      releaseRepository: Mock(ReleaseRepository),
      releasesResponseTransformer: Mock(ReleasesResponseTransformer)
  )

  static final LocalDate NOW = LocalDate.now()
  static final Sort sorting = Sort.by(DESC, "releaseDate", "artist", "albumTitle")
  static final ReleasesResponse response = new ReleasesResponse()

  def "findAllUpcomingReleases paginated: should request all releases from release repository if no artist names are given"() {
    given:
    def page = 1
    def size = 10
    def query = "query"
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllUpcomingReleases([], query, page, size, sorting)

    then:
    1 * underTest.releaseRepository.findAllReleasesFrom(NOW, query, expectedPageRequest)
  }

  def "findAllUpcomingReleases paginated: should transform page result with ReleasesTransformer if no artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAllReleasesFrom(*_) >> pageResult

    when:
    def result = underTest.findAllUpcomingReleases([], "query", 1, 10, sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllUpcomingReleases paginated: should request releases from release repository for given artists"() {
    given:
    def page = 1
    def size = 10
    def query = "query"
    def artistNames = ["A1"]
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllUpcomingReleases(artistNames, query, page, size, sorting)

    then:
    1 * underTest.releaseRepository.findAlleReleasesFromWithArtists(NOW, artistNames, query, expectedPageRequest)
  }

  def "findAllUpcomingReleases paginated: should transform page result with ReleasesTransformer if artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAlleReleasesFromWithArtists(*_) >> pageResult

    when:
    def result = underTest.findAllUpcomingReleases(["A1"], "query", 1, 10, sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllReleasesForTimeRange paginated: should request all releases from release repository if no artist names are given"() {
    given:
    def page = 1
    def size = 10
    def query = "query"
    def timeRange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllReleasesForTimeRange([], timeRange, query, page, size, sorting)

    then:
    1 * underTest.releaseRepository.findAllReleasesBetween(timeRange.from, timeRange.to, query, expectedPageRequest)
  }

  def "findAllReleasesForTimeRange paginated: should transform page result with ReleasesTransformer if no artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAllReleasesBetween(*_) >> pageResult

    when:
    def result = underTest.findAllReleasesForTimeRange([], TimeRange.of(LocalDate.now() - 1, LocalDate.now()), "query", 1, 10, sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllReleasesForTimeRange paginated: should request releases from release repository for given artists"() {
    given:
    def page = 1
    def size = 10
    def artistNames = ["A1"]
    def query = "query"
    def timeRange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllReleasesForTimeRange(artistNames, timeRange, query, page, size, sorting)

    then:
    1 * underTest.releaseRepository.findAllReleasesBetweenWithArtists(artistNames, timeRange.from, timeRange.to, query, expectedPageRequest)
  }

  def "findAllReleasesForTimeRange paginated: should transform page result with ReleasesTransformer if artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)
    ])
    underTest.releaseRepository.findAllReleasesBetweenWithArtists(*_) >> pageResult

    when:
    def result = underTest.findAllReleasesForTimeRange(["A1"], TimeRange.of(LocalDate.now() - 1, LocalDate.now()), "query", 1, 10, sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllReleasesSince paginated: should request all releases since specified date from release repository if no artist names are given"() {
    given:
    def date = LocalDate.now()
    def query = "query"
    def page = 1
    def size = 10
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllReleasesSince([], date, query, page, size, sorting)

    then:
    1 * underTest.releaseRepository.findAllReleasesFrom(date, query, expectedPageRequest)
  }

  def "findAllReleasesSince paginated: should transform release entities with ReleasesTransformer if no artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAllReleasesFrom(*_) >> pageResult

    when:
    def result = underTest.findAllReleasesSince([], LocalDate.now(), "query", 1, 10, sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllReleasesSince paginated: should request all releases since specified date from release repository for given artists"() {
    given:
    def date = LocalDate.now()
    def artists = ["Metallica"]
    def query = "query"
    def page = 1
    def size = 10
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllReleasesSince(artists, date, query, page, size, sorting)

    then:
    1 * underTest.releaseRepository.findAlleReleasesFromWithArtists(date, artists, query, expectedPageRequest)
  }

  def "findAllReleasesSince paginated: should transform release entities with ReleasesTransformer if artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAlleReleasesFromWithArtists(*_) >> pageResult

    when:
    def result = underTest.findAllReleasesSince(["A1"], LocalDate.now(), "query", 1, 10, sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllUpcomingReleases: should request all upcoming releases from release repository if no artist names are given"() {
    when:
    underTest.findAllUpcomingReleases([], sorting)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(NOW - 1, sorting)
  }

  def "findAllUpcomingReleases: should transform release entities with ReleasesTransformer if no artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A2", NOW)]
    underTest.releaseRepository.findAllByReleaseDateAfter(*_) >> releaseEntities

    when:
    def result = underTest.findAllUpcomingReleases([], sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "findAllUpcomingReleases: should request upcoming releases from release repository for given artists"() {
    given:
    def artistNames = ["A1"]

    when:
    underTest.findAllUpcomingReleases(artistNames, sorting)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistInIgnoreCase(NOW - 1, artistNames, sorting)
  }

  def "findAllUpcomingReleases: should transform release entities with ReleasesTransformer if artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByReleaseDateAfterAndArtistInIgnoreCase(*_) >> releaseEntities

    when:
    def result = underTest.findAllUpcomingReleases(["A1"], sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "findAllReleasesForTimeRange: should request all releases for specified time range from release repository if no artist names are given"() {
    given:
    def timeRange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())

    when:
    underTest.findAllReleasesForTimeRange([], timeRange, sorting)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateBetween(timeRange.from, timeRange.to, sorting)
  }

  def "findAllReleasesForTimeRange: should transform release entities with ReleasesTransformer if no artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A2", NOW)]
    underTest.releaseRepository.findAllByReleaseDateBetween(*_) >> releaseEntities

    when:
    def result = underTest.findAllReleasesForTimeRange([], TimeRange.of(LocalDate.now() - 1, LocalDate.now()), sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "findAllReleasesForTimeRange: should request all releases for specified time range from release repository for given artists"() {
    given:
    def artistNames = ["A1"]
    def timeRange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())

    when:
    underTest.findAllReleasesForTimeRange(artistNames, timeRange, sorting)

    then:
    1 * underTest.releaseRepository.findAllByArtistInIgnoreCaseAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to, sorting)
  }

  def "findAllReleasesForTimeRange: should transform release entities with ReleasesTransformer if artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByArtistInIgnoreCaseAndReleaseDateBetween(*_) >> releaseEntities

    when:
    def result = underTest.findAllReleasesForTimeRange(["A1"], TimeRange.of(LocalDate.now() - 1, LocalDate.now()), sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "findAllReleasesSince: should request all releases since specified date from release repository if no artist names are given"() {
    given:
    def date = LocalDate.now()

    when:
    underTest.findAllReleasesSince([], date, sorting)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(date, sorting)
  }

  def "findAllReleasesSince: should transform release entities with ReleasesTransformer if no artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByReleaseDateAfter(*_) >> releaseEntities

    when:
    def result = underTest.findAllReleasesSince([], LocalDate.now(), sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "findAllReleasesSince: should request all releases since specified date from release repository for given artists"() {
    given:
    def date = LocalDate.now()
    def artists = ["Metallica"]

    when:
    underTest.findAllReleasesSince(artists, date, sorting)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistInIgnoreCase(date, artists, sorting)
  }

  def "findAllReleasesSince: should transform release entities with ReleasesTransformer if artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByReleaseDateAfterAndArtistInIgnoreCase(*_) >> releaseEntities

    when:
    def result = underTest.findAllReleasesSince(["A1"], LocalDate.now(), sorting)

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "updateReleaseState: should call release repository to get release"() {
    given:
    long id = 1L

    when:
    underTest.updateReleaseState(id, FAULTY)

    then:
    1 * underTest.releaseRepository.findById(id) >> Optional.of(ReleaseEntityFactory.createReleaseEntity("A"))
  }

  def "updateReleaseState: should throw exception when id not found"() {
    given:
    underTest.releaseRepository.findById(*_) >> Optional.empty()

    when:
    underTest.updateReleaseState(0L, FAULTY)

    then:
    thrown(ResourceNotFoundException)
  }

  def "updateReleaseState: should call release repository to update release if present"() {
    given:
    def id = 1L
    def release = ReleaseEntityFactory.createReleaseEntity("A")
    underTest.releaseRepository.findById(*_) >> Optional.of(release)

    when:
    underTest.updateReleaseState(id, FAULTY)

    then:
    1 * underTest.releaseRepository.save({ args -> args.state == FAULTY })
  }

  def "deleteByReleaseDetailsUrl: should pass release details url to release repository"() {
    given:
    def releaseDetailsUrl = "release-details-url"

    when:
    underTest.deleteByReleaseDetailsUrl(releaseDetailsUrl)

    then:
    1 * underTest.releaseRepository.deleteByReleaseDetailsUrl(releaseDetailsUrl)
  }
}

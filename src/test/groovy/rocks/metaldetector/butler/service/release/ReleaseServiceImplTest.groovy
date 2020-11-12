package rocks.metaldetector.butler.service.release

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.model.release.ReleaseEntityState
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.web.api.ReleasesResponse
import spock.lang.Specification

import java.time.LocalDate

import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory
import static rocks.metaldetector.butler.model.release.ReleaseEntityState.FAULTY
import static rocks.metaldetector.butler.model.release.ReleaseEntityState.OK

class ReleaseServiceImplTest extends Specification {

  ReleaseServiceImpl underTest = new ReleaseServiceImpl(
      releaseRepository: Mock(ReleaseRepository),
      releasesResponseTransformer: Mock(ReleasesResponseTransformer)
  )

  static final LocalDate NOW = LocalDate.now()
  static final Sort sorting = Sort.by(Sort.Direction.ASC, "releaseDate", "artist", "albumTitle")
  static final ReleasesResponse response = new ReleasesResponse()
  static final ReleaseEntityState state = OK

  def "findAllUpcomingReleases paginated: should request all releases from release repository if no artist names are given"() {
    given:
    def page = 1
    def size = 10
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllUpcomingReleases([], state, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndState(NOW - 1, state, expectedPageRequest)
  }

  def "findAllUpcomingReleases paginated: should transform page result with ReleasesTransformer if no artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAllByReleaseDateAfterAndState(*_) >> pageResult

    when:
    def result = underTest.findAllUpcomingReleases([], state, 1, 10)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllUpcomingReleases paginated: should request releases from release repository for given artists"() {
    given:
    def page = 1
    def size = 10
    def artistNames = ["A1"]
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllUpcomingReleases(artistNames, state, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistInAndState(NOW - 1, artistNames, state, expectedPageRequest)
  }

  def "findAllUpcomingReleases paginated: should transform page result with ReleasesTransformer if artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAllByReleaseDateAfterAndArtistInAndState(*_) >> pageResult

    when:
    def result = underTest.findAllUpcomingReleases(["A1"], state, 1, 10)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllReleasesForTimeRange paginated: should request all releases from release repository if no artist names are given"() {
    given:
    def page = 1
    def size = 10
    def timeRange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllReleasesForTimeRange([], timeRange, state, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateBetweenAndState(timeRange.from, timeRange.to, state, expectedPageRequest)
  }

  def "findAllReleasesForTimeRange paginated: should transform page result with ReleasesTransformer if no artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAllByReleaseDateBetweenAndState(*_) >> pageResult

    when:
    def result = underTest.findAllReleasesForTimeRange([], TimeRange.of(LocalDate.now() - 1, LocalDate.now()), state, 1, 10)

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
    def timeRange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllReleasesForTimeRange(artistNames, timeRange, state, page, size)

    then:
    1 * underTest.releaseRepository.findAllByArtistInAndReleaseDateBetweenAndState(artistNames, timeRange.from, timeRange.to, state, expectedPageRequest)
  }

  def "findAllReleasesForTimeRange paginated: should transform page result with ReleasesTransformer if artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)
    ])
    underTest.releaseRepository.findAllByArtistInAndReleaseDateBetweenAndState(*_) >> pageResult

    when:
    def result = underTest.findAllReleasesForTimeRange(["A1"], TimeRange.of(LocalDate.now() - 1, LocalDate.now()), state, 1, 10)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllReleasesSince paginated: should request all releases since specified date from release repository if no artist names are given"() {
    given:
    def date = LocalDate.now()
    def page = 1
    def size = 10
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllReleasesSince([], date, state, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndState(date, state, expectedPageRequest)
  }

  def "findAllReleasesSince paginated: should transform release entities with ReleasesTransformer if no artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAllByReleaseDateAfterAndState(*_) >> pageResult

    when:
    def result = underTest.findAllReleasesSince([], LocalDate.now(), state, 1, 10)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllReleasesSince paginated: should request all releases since specified date from release repository for given artists"() {
    given:
    def date = LocalDate.now()
    def artists = ["Metallica"]
    def page = 1
    def size = 10
    def expectedPageRequest = PageRequest.of(page - 1, size, sorting)

    when:
    underTest.findAllReleasesSince(artists, date, state, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistInAndState(date, artists, state, expectedPageRequest)
  }

  def "findAllReleasesSince paginated: should transform release entities with ReleasesTransformer if artist names are given"() {
    given:
    def pageResult = new PageImpl([
        ReleaseEntityFactory.createReleaseEntity("A1", NOW),
        ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])
    underTest.releaseRepository.findAllByReleaseDateAfterAndArtistInAndState(*_) >> pageResult

    when:
    def result = underTest.findAllReleasesSince(["A1"], LocalDate.now(), state, 1, 10)

    then:
    1 * underTest.releasesResponseTransformer.transformPage(pageResult) >> response

    and:
    result == response
  }

  def "findAllUpcomingReleases: should request all upcoming releases from release repository if no artist names are given"() {
    when:
    underTest.findAllUpcomingReleases([])

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(NOW - 1, sorting)
  }

  def "findAllUpcomingReleases: should transform release entities with ReleasesTransformer if no artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A2", NOW)]
    underTest.releaseRepository.findAllByReleaseDateAfter(*_) >> releaseEntities

    when:
    def result = underTest.findAllUpcomingReleases([])

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "findAllUpcomingReleases: should request upcoming releases from release repository for given artists"() {
    given:
    def artistNames = ["A1"]

    when:
    underTest.findAllUpcomingReleases(artistNames)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(NOW - 1, artistNames, sorting)
  }

  def "findAllUpcomingReleases: should transform release entities with ReleasesTransformer if artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(*_) >> releaseEntities

    when:
    def result = underTest.findAllUpcomingReleases(["A1"])

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "findAllReleasesForTimeRange: should request all releases for specified time range from release repository if no artist names are given"() {
    given:
    def timeRange = TimeRange.of(LocalDate.now() - 1, LocalDate.now())

    when:
    underTest.findAllReleasesForTimeRange([], timeRange)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateBetween(timeRange.from, timeRange.to, sorting)
  }

  def "findAllReleasesForTimeRange: should transform release entities with ReleasesTransformer if no artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A2", NOW)]
    underTest.releaseRepository.findAllByReleaseDateBetween(*_) >> releaseEntities

    when:
    def result = underTest.findAllReleasesForTimeRange([], TimeRange.of(LocalDate.now() - 1, LocalDate.now()))

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
    underTest.findAllReleasesForTimeRange(artistNames, timeRange)

    then:
    1 * underTest.releaseRepository.findAllByArtistInAndReleaseDateBetween(artistNames, timeRange.from, timeRange.to, sorting)
  }

  def "findAllReleasesForTimeRange: should transform release entities with ReleasesTransformer if artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByArtistInAndReleaseDateBetween(*_) >> releaseEntities

    when:
    def result = underTest.findAllReleasesForTimeRange(["A1"], TimeRange.of(LocalDate.now() - 1, LocalDate.now()))

    then:
    1 * underTest.releasesResponseTransformer.transformReleaseEntities(releaseEntities) >> response

    and:
    result == response
  }

  def "findAllReleasesSince: should request all releases since specified date from release repository if no artist names are given"() {
    given:
    def date = LocalDate.now()

    when:
    underTest.findAllReleasesSince([], date)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(date, sorting)
  }

  def "findAllReleasesSince: should transform release entities with ReleasesTransformer if no artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByReleaseDateAfter(*_) >> releaseEntities

    when:
    def result = underTest.findAllReleasesSince([], LocalDate.now())

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
    underTest.findAllReleasesSince(artists, date)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(date, artists, sorting)
  }

  def "findAllReleasesSince: should transform release entities with ReleasesTransformer if artist names are given"() {
    given:
    def releaseEntities = [ReleaseEntityFactory.createReleaseEntity("A1", NOW),
                           ReleaseEntityFactory.createReleaseEntity("A1", NOW + 1)]
    underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(*_) >> releaseEntities

    when:
    def result = underTest.findAllReleasesSince(["A1"], LocalDate.now())

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
    1 * underTest.releaseRepository.findById(id) >> Optional.empty()
  }

  def "updateReleaseState: should call release repository to update release if present"() {
    given:
    def id = 1L
    def release = ReleaseEntityFactory.createReleaseEntity("A")
    underTest.releaseRepository.findById(*_) >> Optional.of(release)

    when:
    underTest.updateReleaseState(id, FAULTY)

    then:
    1 * underTest.releaseRepository.save({args -> args.state == FAULTY})
  }
}

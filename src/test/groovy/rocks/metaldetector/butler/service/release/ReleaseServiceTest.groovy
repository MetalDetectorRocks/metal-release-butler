package rocks.metaldetector.butler.service.release

import org.springframework.data.domain.PageImpl
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.importjob.ImportJobRepository
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.web.dto.CreateImportJobResponse
import rocks.metaldetector.butler.web.dto.ReleaseDto
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.DtoFactory.ReleaseDtoFactory
import static rocks.metaldetector.butler.DtoFactory.ReleaseEntityFactory

class ReleaseServiceTest extends Specification {

  ReleaseServiceImpl underTest = new ReleaseServiceImpl(
      releaseRepository: Mock(ReleaseRepository),
      importJobRepository: Mock(ImportJobRepository),
      metalArchivesReleaseImportService: Mock(MetalArchivesReleaseImporter),
      metalHammerReleaseImportService: Mock(MetalHammerReleaseImporter)
  )

  static LocalDate NOW = LocalDate.now()

  def "importFromExternalSources: should create a new import job before start importing new releases"() {
    when:
    underTest.importFromExternalSources()

    then:
    1 * underTest.importJobRepository.save({
      assert it.jobId != null
      assert it.startTime != null
    }) >> new ImportJobEntity(jobId: UUID.randomUUID())

    then:
    1 * underTest.metalArchivesReleaseImportService.importReleases(*_)

    then:
    1 * underTest.importJobRepository.save({
      assert it.jobId != null
      assert it.startTime != null
    }) >> new ImportJobEntity(jobId: UUID.randomUUID())

    then:
    1 * underTest.metalHammerReleaseImportService.importReleases(*_)
  }

  def "importFromExternalSources: should pass internal import job id to metalArchivesReleaseImportService"() {
    given:
    Long id = 666
    underTest.importJobRepository.save(*_) >> new ImportJobEntity(id: id)

    when:
    underTest.importFromExternalSources()

    then:
    1 * underTest.metalArchivesReleaseImportService.importReleases(id)

    and:
    1 * underTest.metalHammerReleaseImportService.importReleases(id)
  }

  def "importFromExternalSources: should return response with import job id"() {
    given:
    UUID jobId = UUID.randomUUID()
    underTest.importJobRepository.save(*_) >> new ImportJobEntity(jobId: jobId)

    when:
    def result = underTest.importFromExternalSources()

    then:
    result == new CreateImportJobResponse(jobIds: [jobId, jobId])
  }

  def "find all upcoming releases for #artists (paginated)"() {
    given:
    def artists = ["A1", "A2"]
    def page = 1
    def size = 10

    when:
    List<ReleaseDto> results = underTest.findAllUpcomingReleases(artists, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(_, artists, _) >> new PageImpl<>([
            ReleaseEntityFactory.createReleaseEntity("A1", NOW),
            ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])

    and:
    results == [
            ReleaseDtoFactory.createReleaseDto("A1", NOW),
            ReleaseDtoFactory.createReleaseDto("A2", NOW)
    ]
  }

  def "find all upcoming releases (paginated)"() {
    given:
    def artists = []
    def page = 1
    def size = 10

    when:
    List<ReleaseDto> results = underTest.findAllUpcomingReleases(artists, page, size)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(*_) >> new PageImpl<>([
            ReleaseEntityFactory.createReleaseEntity("A1", NOW),
            ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ])

    and:
    results == [
            ReleaseDtoFactory.createReleaseDto("A1", NOW),
            ReleaseDtoFactory.createReleaseDto("A2", NOW)
    ]
  }

  def "find all upcoming releases for #artists"() {
    given:
    def artists = ["A1", "A2"]

    when:
    List<ReleaseDto> results = underTest.findAllUpcomingReleases(artists)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(_, artists) >> [
            ReleaseEntityFactory.createReleaseEntity("A1", NOW),
            ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ]

    and:
    results == [
            ReleaseDtoFactory.createReleaseDto("A1", NOW),
            ReleaseDtoFactory.createReleaseDto("A2", NOW)
    ]
  }

  def "find all upcoming releases"() {
    when:
    List<ReleaseDto> results = underTest.findAllUpcomingReleases([])

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(_) >> [
            ReleaseEntityFactory.createReleaseEntity("A1", NOW),
            ReleaseEntityFactory.createReleaseEntity("A2", NOW)
    ]

    and:
    results == [
            ReleaseDtoFactory.createReleaseDto("A1", NOW),
            ReleaseDtoFactory.createReleaseDto("A2", NOW)
    ]
  }

  @Unroll
  "find all upcoming releases for #artists and between #timeRange"() {
    when:
    List<ReleaseDto> results = underTest.findAllReleasesForTimeRange(artists, timeRange)

    then:
    1 * underTest.releaseRepository.findAllByArtistInAndReleaseDateBetween(artists, timeRange.from, timeRange.to) >> releaseEntities

    and:
    results == expectedReleases

    where:
    artists      | timeRange                                                         | releaseEntities                                                             | expectedReleases
    ["A1"]       | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1))  | [ReleaseEntityFactory.createReleaseEntity("A1", LocalDate.of(2020, 1, 31))] | [ReleaseDtoFactory.createReleaseDto("A1", LocalDate.of(2020, 1, 31))]
    ["A1", "A2"] | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 28)) | getReleaseEntitiesForTimeRangeTest()                                        | getReleaseDtosForTimeRangeTest()
  }

  def "find all upcoming releases between #timeRange"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 28)

    when:
    List<ReleaseDto> results = underTest.findAllReleasesForTimeRange([], TimeRange.of(from, to))

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateBetween(from, to) >> getReleaseEntitiesForTimeRangeTest()

    and:
    results == getReleaseDtosForTimeRangeTest()
  }

  @Unroll
  "count all upcoming releases for #artists"() {
    when:
    long result = underTest.totalCountAllUpcomingReleases(artists)

    then:
    1 * underTest.releaseRepository.countByArtistInAndReleaseDateAfter(artists, _) >> expectedNumber

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
    1 * underTest.releaseRepository.countByReleaseDateAfter(_) >> 1

    and:
    result == 1
  }

  def "count all upcoming releases for #artists and between #timeRange"() {
    when:
    long result = underTest.totalCountAllReleasesForTimeRange(artists, timeRange)

    then:
    1 * underTest.releaseRepository.countByArtistInAndReleaseDateBetween(artists, timeRange.from, timeRange.to) >> expectedNumber

    and:
    result == expectedNumber

    where:
    artists      | timeRange                                                         | expectedNumber
    ["A1"]       | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1))  | 1
    ["A1", "A2"] | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 28)) | 3
  }

  def "count all upcoming releases between #timeRange"() {
    given:
    def from = LocalDate.of(2020, 1, 1)
    def to = LocalDate.of(2020, 2, 1)

    when:
    long result = underTest.totalCountAllReleasesForTimeRange([], TimeRange.of(from, to))

    then:
    1 * underTest.releaseRepository.countByReleaseDateBetween(from, to) >> 1

    and:
    result == 1
  }

  private static List<ReleaseEntity> getReleaseEntitiesForTimeRangeTest() {
    return [ReleaseEntityFactory.createReleaseEntity("A1", LocalDate.of(2020, 1, 31)),
            ReleaseEntityFactory.createReleaseEntity("A1", LocalDate.of(2020, 2, 28)),
            ReleaseEntityFactory.createReleaseEntity("A2", LocalDate.of(2020, 3, 31))]
  }

  private static List<ReleaseDto> getReleaseDtosForTimeRangeTest() {
    return [ReleaseDtoFactory.createReleaseDto("A1", LocalDate.of(2020, 1, 31)),
            ReleaseDtoFactory.createReleaseDto("A1", LocalDate.of(2020, 2, 28)),
            ReleaseDtoFactory.createReleaseDto("A2", LocalDate.of(2020, 3, 31))]
  }
}

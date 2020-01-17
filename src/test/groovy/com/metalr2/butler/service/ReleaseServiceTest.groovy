package com.metalr2.butler.service

import com.metalr2.butler.model.TimeRange
import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseRepository
import com.metalr2.butler.service.converter.Converter
import com.metalr2.butler.supplier.metalarchives.MetalArchivesRestClient
import com.metalr2.butler.web.dto.ReleaseDto
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static com.metalr2.butler.DtoFactory.ReleaseDtoFactory
import static com.metalr2.butler.DtoFactory.ReleaseEntityFactory

class ReleaseServiceTest extends Specification {

  ReleaseService underTest = new ReleaseServiceImpl(Mock(ReleaseRepository), Mock(MetalArchivesRestClient), Mock(Converter))
  static LocalDate now = LocalDate.now()

  @Unroll
  def "find all upcoming releases for #artists"() {
    when:
    List<ReleaseDto> results = underTest.findAllUpcomingReleases(artists)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(_, artists) >> releaseEntities

    and:
    results == expectedReleases

    where:
    artists << [["A1"], ["A0", "A1", "A2"]]
    releaseEntities << [[ReleaseEntityFactory.createReleaseEntity("A1", now)], [
        ReleaseEntityFactory.createReleaseEntity("A1", now),
        ReleaseEntityFactory.createReleaseEntity("A2", now),
        ReleaseEntityFactory.createReleaseEntity("A3", now)]]
    expectedReleases << [[ReleaseDtoFactory.createReleaseDto("A1", now)],
                         [ReleaseDtoFactory.createReleaseDto("A1", now),
                          ReleaseDtoFactory.createReleaseDto("A2", now),
                          ReleaseDtoFactory.createReleaseDto("A3", now)]]
  }

  def "find all upcoming releases"() {
    when:
    List<ReleaseDto> results = underTest.findAllUpcomingReleases([])

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(_) >> [ReleaseEntityFactory.createReleaseEntity("A1", now),
                                                                     ReleaseEntityFactory.createReleaseEntity("A2", now),
                                                                     ReleaseEntityFactory.createReleaseEntity("A3", now)]

    and:
    results == [ReleaseDtoFactory.createReleaseDto("A1", now),
                ReleaseDtoFactory.createReleaseDto("A2", now),
                ReleaseDtoFactory.createReleaseDto("A3", now)]
  }

  @Unroll
  def "find all upcoming releases for #artists and between #timeRange"() {
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
  def "count all upcoming releases for #artists"() {
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

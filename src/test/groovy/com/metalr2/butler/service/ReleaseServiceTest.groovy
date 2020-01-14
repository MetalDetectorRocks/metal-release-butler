package com.metalr2.butler.service

import com.metalr2.butler.model.TimeRange
import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseRepository
import com.metalr2.butler.supplier.metalarchives.MetalArchivesRestClient
import com.metalr2.butler.service.converter.Converter
import com.metalr2.butler.web.dto.ReleaseDto
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static com.metalr2.butler.DtoFactory.ReleaseEntityFactory
import static com.metalr2.butler.DtoFactory.ReleaseDtoFactory

class ReleaseServiceTest extends Specification {

  ReleaseService underTest = new ReleaseServiceImpl(Mock(ReleaseRepository), Mock(MetalArchivesRestClient), Mock(Converter))
  static LocalDate now = LocalDate.now()

  @Unroll
  def "find all upcoming releases for #artists"() {
    when:
    List<ReleaseDto> results = underTest.findAllUpcomingReleases(artists)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfterAndArtistIn(*_) >> ReleaseEntityFactory.multiple(3, now)

    and:
    results.containsAll(expectedReleases)

    where:
    artists            | expectedReleases
    ["A1"]             | [ReleaseDtoFactory.one("A1", now)]
    ["A0", "A1", "A2"] | ReleaseDtoFactory.multiple(3, now)
  }

  def "find all upcoming releases"() {
    when:
    List<ReleaseDto> results = underTest.findAllUpcomingReleases(artists)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateAfter(_) >> ReleaseEntityFactory.multiple(3, now)

    and:
    results.containsAll(expectedReleases)

    where:
    artists | expectedReleases
    []      | ReleaseDtoFactory.multiple(3, now)
  }

  @Unroll
  def "find all upcoming releases for #artists and between #timeRange"() {
    when:
    List<ReleaseDto> results = underTest.findAllReleasesForTimeRange(artists, timeRange)

    then:
    1 * underTest.releaseRepository.findAllByArtistInAndReleaseDateBetween(*_) >> getReleaseEntitiesForTimeRangeTest()

    and:
    results.containsAll(expectedReleases)

    where:
    artists      | timeRange                                                         | expectedReleases
    ["A1"]       | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1))  | [ReleaseDtoFactory.one("A1", LocalDate.of(2020, 1, 31))]
    ["A1", "A2"] | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 28)) | getReleaseDtosForTimeRangeTest()
  }

  def "find all upcoming releases between #timeRange"() {
    when:
    List<ReleaseDto> results = underTest.findAllReleasesForTimeRange(artists, timeRange)

    then:
    1 * underTest.releaseRepository.findAllByReleaseDateBetween(*_) >> getReleaseEntitiesForTimeRangeTest()

    and:
    results.containsAll(expectedReleases)

    where:
    artists | timeRange                                                         | expectedReleases
    []      | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 28)) | getReleaseDtosForTimeRangeTest()
  }

  @Unroll
  def "count all upcoming releases for #artists"() {
    when:
    long result = underTest.totalCountAllUpcomingReleases(artists)

    then:
    1 * underTest.releaseRepository.countByArtistInAndReleaseDateAfter(*_) >> expectedNumber

    and:
    result == expectedNumber

    where:
    artists            | expectedNumber
    ["A0", "A1", "A2"] | 3
    ["A0", "A1"]       | 2
  }

  def "count all upcoming releases"() {
    when:
    long result = underTest.totalCountAllUpcomingReleases(artists)

    then:
    1 * underTest.releaseRepository.countByReleaseDateAfter(*_) >> expectedNumber

    and:
    result == expectedNumber

    where:
    artists | expectedNumber
    []      | 0
  }

  def "count all upcoming releases for #artists and between #timeRange"() {
    when:
    long result = underTest.totalCountAllReleasesForTimeRange(artists, timeRange)

    then:
    1 * underTest.releaseRepository.countByArtistInAndReleaseDateBetween(*_) >> expectedNumber

    and:
    result == expectedNumber

    where:
    artists      | timeRange                                                         | expectedNumber
    ["A1"]       | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1))  | 1
    ["A1", "A2"] | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 28)) | 3
  }

  def "count all upcoming releases between #timeRange"() {
    when:
    long result = underTest.totalCountAllReleasesForTimeRange(artists, timeRange)

    then:
    1 * underTest.releaseRepository.countByReleaseDateBetween(*_) >> expectedNumber

    and:
    result == expectedNumber

    where:
    artists | timeRange                                                        | expectedNumber
    []      | TimeRange.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1)) | 1
  }

  private List<ReleaseEntity> getReleaseEntitiesForTimeRangeTest() {
    return [ReleaseEntityFactory.one("A1", LocalDate.of(2020, 1, 31)),
            ReleaseEntityFactory.one("A1", LocalDate.of(2020, 2, 28)),
            ReleaseEntityFactory.one("A2", LocalDate.of(2020, 3, 31))]
  }

  private List<ReleaseDto> getReleaseDtosForTimeRangeTest() {
    return [ReleaseDtoFactory.one("A1", LocalDate.of(2020, 1, 31)),
            ReleaseDtoFactory.one("A1", LocalDate.of(2020, 2, 28)),
            ReleaseDtoFactory.one("A2", LocalDate.of(2020, 3, 31))]
  }
}

package com.metalr2.butler.service

import com.metalr2.butler.web.dto.ReleaseDto

import java.time.LocalDate

interface ReleaseService {

  void saveAll(List<String[]> upcomingReleasesRawData)

  List<ReleaseDto> findAllUpcomingReleases(int page, int size)

  List<ReleaseDto> findAllReleasesInTimeRange(LocalDate from, LocalDate to, int page, int size)

  List<ReleaseDto> findAllUpcomingReleasesForArtists(Iterable<String> artistNames, int page, int size)

  List<ReleaseDto> findAllReleasesInTimeRangeForArtists(Iterable<String> artistNames, LocalDate from, LocalDate to, int page, int size)

  long totalCountAllUpcomingReleases()

  long totalCountAllReleasesInTimeRange(LocalDate from, LocalDate to)

  long totalCountAllUpcomingReleasesForArtists(Iterable<String> artistNames)

  long totalCountAllReleasesInTimeRangeForArtists(Iterable<String> artistNames, LocalDate from, LocalDate to)

}
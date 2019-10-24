package com.metalr2.butler.service

import com.metalr2.butler.web.dto.ReleaseDto

import java.time.LocalDate

interface ReleaseService {

  void saveAll(List<String[]> upcomingReleasesRawData)

  List<ReleaseDto> findAllUpcomingReleases(int page, int size)

  List<ReleaseDto> findAllReleasesInTimeRange(LocalDate from, LocalDate to, int page, int size)

  List<ReleaseDto> findAllUpcomingReleasesForArtists(List<String> artistNames, int page, int size)

  List<ReleaseDto> findAllReleasesInTimeRangeForArtists(List<String> artistNames, LocalDate from, LocalDate to, int page, int size)

  long totalCountAllUpcomingReleases()

  long totalCountAllReleasesInTimeRange()

  long totalCountAllUpcomingReleasesForArtists()

  long totalCountAllReleasesInTimeRangeForArtists()

}
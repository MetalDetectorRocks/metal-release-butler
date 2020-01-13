package com.metalr2.butler.service

import com.metalr2.butler.model.TimeRange
import com.metalr2.butler.web.dto.ReleaseDto
import com.metalr2.butler.web.dto.ReleaseImportResponse

interface ReleaseService {

  ReleaseImportResponse importFromExternalSource();

  // ToDo DanielW: Cache?
  List<ReleaseDto> findAllUpcomingReleases(Iterable<String> artistNames, int page, int size)

  List<ReleaseDto> findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, int page, int size)

  List<ReleaseDto> findAllUpcomingReleases(Iterable<String> artistNames)

  List<ReleaseDto> findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange)

  long totalCountAllUpcomingReleases(Iterable<String> artistNames)

  long totalCountAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange)

}

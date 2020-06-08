package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.web.dto.CreateImportJobResponse
import rocks.metaldetector.butler.web.dto.ReleaseDto

interface ReleaseService {

  CreateImportJobResponse importFromExternalSources()

  List<ReleaseDto> findAllUpcomingReleases(Iterable<String> artistNames, int page, int size)

  List<ReleaseDto> findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, int page, int size)

  List<ReleaseDto> findAllUpcomingReleases(Iterable<String> artistNames)

  List<ReleaseDto> findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange)

  long totalCountAllUpcomingReleases(Iterable<String> artistNames)

  long totalCountAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange)

}

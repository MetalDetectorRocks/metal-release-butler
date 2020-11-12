package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.model.release.ReleaseEntityState
import rocks.metaldetector.butler.web.api.ReleasesResponse

import java.time.LocalDate

interface ReleaseService {

  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames, ReleaseEntityState state, int page, int size)

  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, ReleaseEntityState state, int page, int size)

  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom, ReleaseEntityState state, int page, int size)

  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames)

  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange)

  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom)

}

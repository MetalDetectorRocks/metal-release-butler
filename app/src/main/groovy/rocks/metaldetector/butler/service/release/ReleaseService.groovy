package rocks.metaldetector.butler.service.release

import org.springframework.data.domain.Sort
import rocks.metaldetector.butler.persistence.domain.TimeRange
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState
import rocks.metaldetector.butler.web.api.ReleasesResponse

import java.time.LocalDate

interface ReleaseService {

  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames, String query, int page, int size, Sort sorting)

  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, String query, int page, int size, Sort sorting)

  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom, String query, int page, int size, Sort sorting)

  ReleasesResponse findAllUpcomingReleases(Iterable<String> artistNames, Sort sorting)

  ReleasesResponse findAllReleasesForTimeRange(Iterable<String> artistNames, TimeRange timeRange, Sort sorting)

  ReleasesResponse findAllReleasesSince(Iterable<String> artistNames, LocalDate dateFrom, Sort sorting)

  void updateReleaseState(long releaseId, ReleaseEntityState state)

  void deleteByReleaseDetailsUrl(String releaseDetailsUrl)

}

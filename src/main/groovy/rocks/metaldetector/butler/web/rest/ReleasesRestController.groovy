package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.service.release.ReleaseService
import rocks.metaldetector.butler.web.dto.ReleasesRequest
import rocks.metaldetector.butler.web.dto.ReleasesRequestPaginated
import rocks.metaldetector.butler.web.dto.ReleasesResponse

import javax.validation.Valid

import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASES
import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASES_UNPAGINATED

@RestController
class ReleasesRestController {

  @Autowired
  ReleaseService releaseService

  @PostMapping(path = RELEASES, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_USER')")
  ResponseEntity<ReleasesResponse> getPaginatedReleases(@Valid @RequestBody ReleasesRequestPaginated request) {
    def releases
    def totalReleases

    if (request.dateFrom == null && request.dateTo == null) {
      totalReleases = releaseService.totalCountAllUpcomingReleases(request.artists)
      releases = releaseService.findAllUpcomingReleases(request.artists, request.page, request.size)
    }
    else if (request.dateFrom != null && request.dateTo != null) {
      TimeRange timeRange = TimeRange.of(request.dateFrom, request.dateTo)
      totalReleases = releaseService.totalCountAllReleasesForTimeRange(request.artists, timeRange)
      releases = releaseService.findAllReleasesForTimeRange(request.artists, timeRange, request.page, request.size)
    }
    else {
      throw new IllegalArgumentException("The parameters 'dateFrom' and 'dateTo' must both have a valid date value in the format YYYY-MM-DD.")
    }

    def response = new ReleasesResponse(currentPage: request.getPage(), size: request.getSize(),
                                        totalPages: Math.ceil(totalReleases / request.getSize()),
                                        totalReleases: totalReleases, releases: releases)
    return ResponseEntity.ok(response)
  }

  @PostMapping(path = RELEASES_UNPAGINATED, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  ResponseEntity<ReleasesResponse> getAllReleases(@Valid @RequestBody ReleasesRequest request) {
    def releases

    if (request.dateFrom == null) {
      releases = releaseService.findAllUpcomingReleases([])
    }
    else {
      releases = releaseService.findAllReleasesSince([], TimeRange.of(request.dateFrom, null)) // ToDo DanielW: Create method
    }

    def response = new ReleasesResponse(currentPage: 1, size: releases.size(),
                                        totalPages: 1,
                                        totalReleases: releases.size(), releases: releases)
    return ResponseEntity.ok(response)
  }
}

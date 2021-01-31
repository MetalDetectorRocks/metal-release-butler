package rocks.metaldetector.butler.web.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import rocks.metaldetector.butler.model.TimeRange
import rocks.metaldetector.butler.service.release.ReleaseService
import rocks.metaldetector.butler.web.api.ReleaseUpdateRequest
import rocks.metaldetector.butler.web.api.ReleasesRequest
import rocks.metaldetector.butler.web.api.ReleasesRequestPaginated
import rocks.metaldetector.butler.web.api.ReleasesResponse

import javax.validation.Valid

import static org.springframework.data.domain.Sort.Direction.ASC
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASES
import static rocks.metaldetector.butler.config.constants.Endpoints.RELEASES_UNPAGINATED
import static rocks.metaldetector.butler.config.constants.Endpoints.UPDATE_RELEASE

@RestController
class ReleasesRestController {

  @Autowired
  ReleaseService releaseService

  @PostMapping(path = RELEASES, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_USER')")
  ResponseEntity<ReleasesResponse> getPaginatedReleases(@Valid @RequestBody ReleasesRequestPaginated request,
                                                        @SortDefault(sort = ["releaseDate", "artist", "albumTitle"], direction = ASC) Sort sorting) {
    def releasesResponse
    def query = request.query != null ? request.query.trim() : ""
    def artists = request.artists ? request.artists.collect { it?.toLowerCase() } - null : []

    if (request.dateFrom == null && request.dateTo == null) {
      releasesResponse = releaseService.findAllUpcomingReleases(artists, query, request.page, request.size, sorting)
    }
    else if (request.dateFrom != null && request.dateTo != null) {
      TimeRange timeRange = TimeRange.of(request.dateFrom, request.dateTo)
      releasesResponse = releaseService.findAllReleasesForTimeRange(artists, timeRange, query, request.page, request.size, sorting)
    }
    else if (request.dateFrom != null) {
      releasesResponse = releaseService.findAllReleasesSince(artists, request.dateFrom, query, request.page, request.size, sorting)
    }
    else {
      throw new IllegalArgumentException("Please specify a valid date for 'dateFrom' and 'dateTo' or only for 'dateFrom' with format 'YYYY-MM-DD'.")
    }

    return ResponseEntity.ok(releasesResponse)
  }

  @PutMapping(path = UPDATE_RELEASE, consumes = APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  ResponseEntity<Void> updateReleaseState(@Valid @RequestBody ReleaseUpdateRequest request, @PathVariable long releaseId) {
    releaseService.updateReleaseState(releaseId, request.state)
    return ResponseEntity.ok().build()
  }

  @PostMapping(path = RELEASES_UNPAGINATED, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  ResponseEntity<ReleasesResponse> getAllReleases(@Valid @RequestBody ReleasesRequest request,
                                                  @SortDefault(sort = ["releaseDate", "artist", "albumTitle"], direction = ASC) Sort sorting) {
    def releasesResponse
    def artists = request.artists ? request.artists.collect { it?.toLowerCase() } - null : []

    if (request.dateFrom == null && request.dateTo == null) {
      releasesResponse = releaseService.findAllUpcomingReleases(artists, sorting)
    }
    else if (request.dateFrom != null && request.dateTo != null) {
      releasesResponse = releaseService.findAllReleasesForTimeRange(artists, TimeRange.of(request.dateFrom, request.dateTo), sorting)
    }
    else if (request.dateFrom != null) {
      releasesResponse = releaseService.findAllReleasesSince(artists, request.dateFrom, sorting)
    }
    else {
      throw new IllegalArgumentException("Please specify a valid date for 'dateFrom' and 'dateTo' or only for 'dateFrom' with format 'YYYY-MM-DD'.")
    }

    return ResponseEntity.ok(releasesResponse)
  }
}

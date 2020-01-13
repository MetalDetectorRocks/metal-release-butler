package com.metalr2.butler.web.rest

import com.metalr2.butler.config.Endpoints
import com.metalr2.butler.model.TimeRange
import com.metalr2.butler.service.ReleaseService
import com.metalr2.butler.web.dto.ReleaseImportResponse
import com.metalr2.butler.web.dto.ReleasesRequest
import com.metalr2.butler.web.dto.ReleasesRequestPaginated
import com.metalr2.butler.web.dto.ReleasesResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@RestController
@RequestMapping(Endpoints.RELEASES)
class ReleasesRestController {

  static final IMPORT_ACTION = "import"

  final ReleaseService releaseService

  ReleasesRestController(ReleaseService releaseService) {
    this.releaseService = releaseService
  }

  @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<ReleaseImportResponse> importReleases(@RequestParam("action") String action) {
    if (action == IMPORT_ACTION) {
      ReleaseImportResponse response = releaseService.importFromExternalSource()
      return ResponseEntity.ok(response)
    }
    else {
      throw new IllegalArgumentException("Only query param 'action=import' is supported for GET request!")
    }
  }

  @PostMapping(path = ["paginated"], consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<ReleasesResponse> getPaginatedReleases(@Valid @RequestBody ReleasesRequestPaginated request) {
    def releases

    if (request.dateFrom == null && request.dateTo == null) {
      releases = releaseService.findAllUpcomingReleases(request.artists, request.page, request.size)
    }
    else if (request.dateFrom != null && request.dateTo != null) {
      releases = releaseService.findAllReleasesForTimeRange(request.artists, TimeRange.of(request.dateFrom, request.dateTo), request.page, request.size)
    }
    else {
      throw new IllegalArgumentException("The parameters 'dateFrom' and 'dateTo' must both have a valid date value in the format YYYY-MM-DD.")
    }

    def response = new ReleasesResponse(currentPage: request.getPage(), size: request.getSize(),
                                        totalPages: Math.ceil(releases.size() / request.getSize()),
                                        totalReleases: releases.size(), releases: releases)
    return ResponseEntity.ok(response)
  }

  @PostMapping(path = ["","/"], consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<ReleasesResponse> getAllReleases(@Valid @RequestBody ReleasesRequest request) {
    def releases

    if (request.dateFrom == null && request.dateTo == null) {
      releases = releaseService.findAllUpcomingReleases(request.artists)
    }
    else if (request.dateFrom != null && request.dateTo != null) {
      releases = releaseService.findAllReleasesForTimeRange(request.artists, TimeRange.of(request.dateFrom, request.dateTo))
    }
    else {
      throw new IllegalArgumentException("The parameters 'dateFrom' and 'dateTo' must both have a valid date value in the format YYYY-MM-DD.")
    }

    def response = new ReleasesResponse(currentPage: 1, size: releases.size(),
                                        totalPages: 1,
                                        totalReleases: releases.size(), releases: releases)
    return ResponseEntity.ok(response)
  }

}

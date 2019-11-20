package com.metalr2.butler.web.rest

import com.metalr2.butler.config.Endpoints
import com.metalr2.butler.model.TimeRange
import com.metalr2.butler.service.ReleaseService
import com.metalr2.butler.web.dto.ReleaseImportResponse
import com.metalr2.butler.web.dto.ReleasesRequest
import com.metalr2.butler.web.dto.ReleasesResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

  @PostMapping(path = ["", "/"], consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<ReleasesResponse> getReleases(@Valid @RequestBody ReleasesRequest request) {
    def totalReleases
    def releases

    if (request.dateFrom == null && request.dateTo == null) {
      totalReleases = releaseService.totalCountAllUpcomingReleases(request.artists)
      releases = releaseService.findAllUpcomingReleases(request.artists, request.page, request.size)
    }
    else if (request.dateFrom != null && request.dateTo != null) {
      totalReleases = releaseService.totalCountAllReleasesForTimeRange(request.artists, TimeRange.of(request.dateFrom, request.dateTo))
      releases = releaseService.findAllReleasesForTimeRange(request.artists, TimeRange.of(request.dateFrom, request.dateTo), request.page, request.size)
    }
    else {
      throw new IllegalArgumentException("The parameters 'dateFrom' and 'dateTo' must both have a valid date value in the format YYYY-MM-DD.")
    }

    def response = new ReleasesResponse(currentPage: request.getPage(), size: request.getSize(),
                                        totalPages: Math.ceil(totalReleases / request.getSize()),
                                        totalReleases: totalReleases, releases: releases)

    return ResponseEntity.ok(response)
  }

}

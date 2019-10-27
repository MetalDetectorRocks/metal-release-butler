package com.metalr2.butler.web.rest

import com.metalr2.butler.service.ReleaseService
import com.metalr2.butler.web.dto.UpcomingReleasesRequest
import com.metalr2.butler.web.dto.UpcomingReleasesResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.time.LocalDate

import static org.springframework.format.annotation.DateTimeFormat.ISO

@RestController
@RequestMapping("/rest/v1/releases")
class UpcomingReleasesRestController {

  final ReleaseService releaseService

  UpcomingReleasesRestController(ReleaseService releaseService) {
    this.releaseService = releaseService
  }

  @GetMapping(path = ["", "/", "/all"], produces = [MediaType.APPLICATION_JSON_VALUE])
  ResponseEntity<UpcomingReleasesResponse> getAllUpcomingReleases(@RequestParam(name = "page", defaultValue = "1") int page,
                                                                  @RequestParam(name = "size", defaultValue = "1") int size,
                                                                  @RequestParam(name = "from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                                  @RequestParam(name = "to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
    def totalReleases
    def releases

    if (from == null && to == null) {
      totalReleases = releaseService.totalCountAllUpcomingReleases()
      releases = releaseService.findAllUpcomingReleases(page, size)
    }
    else if (from != null && to != null) {
      totalReleases = releaseService.totalCountAllReleasesInTimeRange(from, to)
      releases = releaseService.findAllReleasesInTimeRange(from, to, page, size)
    }
    else {
      throw new IllegalArgumentException("The parameters 'from' and 'to' must both have a valid date value in the format YYYY-MM-DD.")
    }

    def response = new UpcomingReleasesResponse(currentPage: page, size: size, totalPages: Math.ceil(totalReleases / size),
                                                totalReleases: totalReleases, releases: releases)
    return ResponseEntity.ok(response)
  }

  @PostMapping(path = "/my-artists", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  ResponseEntity<UpcomingReleasesResponse> getAllUpcomingReleasesForArtists(@RequestParam(name = "page", defaultValue = "1") int page,
                                                                            @RequestParam(name = "size", defaultValue = "1") int size,
                                                                            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                                                            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
                                                                            @RequestBody UpcomingReleasesRequest request) {
    def totalReleases
    def releases

    if (from == null && to == null) {
      totalReleases = releaseService.totalCountAllUpcomingReleasesForArtists(request.artistNames)
      releases = releaseService.findAllUpcomingReleasesForArtists(request.artistNames, page, size)
    }
    else if (from != null && to != null) {
      totalReleases = releaseService.totalCountAllReleasesInTimeRangeForArtists(request.artistNames, from, to)
      releases = releaseService.findAllReleasesInTimeRangeForArtists(request.artistNames, from, to, page, size)
    }
    else {
      throw new IllegalArgumentException("The parameters 'from' and 'to' must both have a valid date value in the format YYYY-MM-DD.")
    }

    def response = new UpcomingReleasesResponse(currentPage: page, size: size, totalPages: Math.ceil(totalReleases / size),
                                                totalReleases: totalReleases, releases: releases)
    return ResponseEntity.ok(response)
  }

}

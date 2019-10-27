package com.metalr2.butler.web.rest

import com.metalr2.butler.service.ReleaseService
import com.metalr2.butler.web.dto.UpcomingReleasesResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.time.LocalDate

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
                                                                  @RequestParam(name = "from", required = false) LocalDate from,
                                                                  @RequestParam(name = "to", required = false) LocalDate to) {
    def totalReleases = 0
    def releases = []

    if (from == null && to == null) {
      totalReleases = releaseService.totalCountAllUpcomingReleases()
      releases = releaseService.findAllUpcomingReleases(page, size)
    }
    else if (from != null && to != null) {
      // ToDo DanielW: Testen!
      totalReleases = releaseService.totalCountAllReleasesInTimeRange(from, to)
      releases = releaseService.findAllReleasesInTimeRange(from, to, page, size)
    }
    else {
      throw new IllegalArgumentException("The parameters 'from' and 'to' must both have a valid date value in the format YYYY-MM-DD.")
    }

    // ToDo DanielW: totalPages bei verschiedenen Szenarien prüfen (Bsp. 11 releases, size = 10)
    def response = new UpcomingReleasesResponse(currentPage: page, size: size, totalPages: Math.max(1, (totalReleases / size) as int),
                                                totalReleases: totalReleases, releases: releases)
    return ResponseEntity.ok(response)
  }

  @PostMapping(path = "/my-artists", produces = [MediaType.APPLICATION_JSON_VALUE])
  ResponseEntity<UpcomingReleasesResponse> getAllUpcomingReleasesForArtists(@RequestParam(name = "page", defaultValue = "1") int page,
                                                                            @RequestParam(name = "size", defaultValue = "1") int size,
                                                                            @RequestParam(name = "from", required = false) LocalDate from,
                                                                            @RequestParam(name = "to", required = false) LocalDate to) {
//    def artistNames = []
//    def releases = []
//
//    if (from == null && to == null) {
//      releases = releaseService.findAllUpcomingReleasesForArtists(artistNames, page, size)
//    }
//    else if (from != null || to != null) {
//      releases = releaseService.findAllReleasesInTimeRangeForArtists(artistNames, from, to, page, size)
//    }
//
//    return ResponseEntity.ok(releases)
  }

}

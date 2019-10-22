package com.metalr2.butler.web.rest

import com.metalr2.butler.service.ReleaseService
import com.metalr2.butler.service.restclient.MetalArchivesRestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/")
class DemoRestController {

  final MetalArchivesRestClient restClient
  final ReleaseService releaseService

  @Autowired
  DemoRestController(MetalArchivesRestClient restClient, ReleaseService releaseService) {
    this.restClient = restClient
    this.releaseService = releaseService
  }

  @GetMapping
  ResponseEntity<String> demo() {
    List<String[]> response = restClient.requestReleases()
    releaseService.saveAll(response)

    ResponseEntity.ok("Done")
  }

}

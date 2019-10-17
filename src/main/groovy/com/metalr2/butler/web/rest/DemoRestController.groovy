package com.metalr2.butler.web.rest

import com.metalr2.butler.service.restclient.MetalArchivesRestClient
import com.metalr2.butler.web.dto.ReleaseDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/")
class DemoRestController {

  final MetalArchivesRestClient restClient

  @Autowired
  DemoRestController(MetalArchivesRestClient restClient) {
    this.restClient = restClient
  }

  @GetMapping
  ResponseEntity<List<ReleaseDto>> demo() {
    List<ReleaseDto> response = restClient.requestReleases()
    ResponseEntity.ok(response)
  }

}

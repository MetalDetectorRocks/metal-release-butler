package com.metalr2.butler.supplier.metalarchives

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MetalArchivesRestClient {

  static final String UPCOMING_RELEASES_URL = "https://www.metal-archives.com/release/ajax-upcoming/json/1?sEcho=1&iDisplayStart={startOfRange}"

  final RestTemplate restTemplate

  @Autowired
  MetalArchivesRestClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate
  }

  List<String[]> requestReleases() {
    /*
     * The REST-interface of metal-archives.com responses a maximum of 100 records per request.
     * Therefore we have to request 100 records each until we don't get any more results.
     */
    List<String[]> rawResponse = []
    def dataAvailable = true
    def startOfRange = 0

    while (dataAvailable) {
      // (1) request
      ResponseEntity<MetalArchivesResponse> responseEntity = restTemplate.getForEntity(UPCOMING_RELEASES_URL
              , MetalArchivesResponse.class
              , startOfRange)

      // (2) check http status and response body
      MetalArchivesResponse responseBody = responseEntity.body
      if (responseEntity.statusCode != HttpStatus.OK || responseBody == null) {
        break
      }

      // (3) collect raw response data
      rawResponse.addAll(responseBody.data)

      // (4) prepare next iteration
      if ((startOfRange + 100) < responseBody.totalRecords) {
        startOfRange += 100
      }
      else {
        dataAvailable = false
      }
    }

    return rawResponse
  }

}

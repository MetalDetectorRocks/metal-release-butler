package com.metalr2.butler.service.restclient

import com.metalr2.butler.service.converter.Converter
import com.metalr2.butler.web.dto.ReleaseDto
import com.metalr2.butler.web.dto.UpcomingReleasesResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MetalArchivesRestClient {

  static final String UPCOMING_RELEASES_URL = "https://www.metal-archives.com/release/ajax-upcoming/json/1?sEcho=1&iDisplayStart={startOfRange}"

  final RestTemplate restTemplate
  final Converter<String[], List<ReleaseDto>> converter

  @Autowired
  MetalArchivesRestClient(RestTemplate restTemplate, Converter<String[], List<ReleaseDto>> converter) {
    this.restTemplate = restTemplate
    this.converter = converter
  }

  List<ReleaseDto> requestReleases() {
    /*
     * The REST-interface of metal-archives.com responses a maximum of 100 records per request.
     * Therefore we have to request 100 records each until we don't get any more results.
     */
    List<String[]> rawResponse = []
    def dataAvailable = true
    def startOfRange = 0

    while (dataAvailable) {
      // (1) request
      ResponseEntity<UpcomingReleasesResponse> responseEntity = restTemplate.getForEntity(UPCOMING_RELEASES_URL
              , UpcomingReleasesResponse.class
              , startOfRange)

      // (2) check http status and response body
      UpcomingReleasesResponse responseBody = responseEntity.body
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

    return convertResults(rawResponse)
  }

  private List<ReleaseDto> convertResults(List<String[]> rawResponse) {
    List<ReleaseDto> results = []
    rawResponse.each { results.addAll(converter.convert(it)) }

    return results
  }

}

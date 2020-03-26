package rocks.metaldetector.butler.supplier.metalarchives

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MetalArchivesRestClient {

  static final String UPCOMING_RELEASES_URL = "https://www.metal-archives.com/release/ajax-upcoming/json/1?sEcho=1&iDisplayStart={startOfRange}"
  static final int MAX_ATTEMPTS = 5

  @Autowired
  RestTemplate restTemplate

  /*
   * The REST-interface of metal-archives.com responds only with a list of strings for each release.
   * The information on band, album name etc. is determined by the order of the string in the array.
   */
  List<String[]> requestReleases() {
    List<String[]> rawResponse = []
    def dataAvailable = true
    def startOfRange = 0
    def attempt = 0

    // The REST endpoint of metal archives responds a maximum of 100 records per request
    while (dataAvailable) {
      // (1) request
      ResponseEntity<MetalArchivesResponse> responseEntity = restTemplate.getForEntity(UPCOMING_RELEASES_URL,
                                                                                       MetalArchivesResponse,
                                                                                       startOfRange)

      // (2) check http status and response body
      MetalArchivesResponse responseBody = responseEntity.body
      if (responseEntity.statusCode != HttpStatus.OK || responseBody == null) {
        if (++attempt < MAX_ATTEMPTS) {
          continue
        }
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
      attempt = 0
    }

    return rawResponse
  }
}

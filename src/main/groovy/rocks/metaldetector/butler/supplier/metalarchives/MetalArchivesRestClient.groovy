package rocks.metaldetector.butler.supplier.metalarchives

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import static org.springframework.http.HttpStatus.OK

@Service
@Slf4j
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
      ResponseEntity<MetalArchivesReleasesResponse> responseEntity = restTemplate.getForEntity(UPCOMING_RELEASES_URL,
                                                                                               MetalArchivesReleasesResponse,
                                                                                               startOfRange)

      // (2) check http status and response body
      MetalArchivesReleasesResponse responseBody = responseEntity.body
      if (responseEntity.statusCode != OK || responseBody == null) {
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
        attempt = 0
      }
      else {
        dataAvailable = false
      }
    }

    return rawResponse
  }
}

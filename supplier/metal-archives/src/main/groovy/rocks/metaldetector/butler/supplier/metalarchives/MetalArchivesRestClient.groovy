package rocks.metaldetector.butler.supplier.metalarchives

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations

import java.time.LocalDate

@Service
@Slf4j
class MetalArchivesRestClient {

  static final String UPCOMING_RELEASES_URL = "https://www.metal-archives.com/release/ajax-upcoming/json/1?sEcho=1&iDisplayStart={startOfRange}&fromDate={fromDate}"
  static final int MAX_ATTEMPTS = 5

  @Autowired
  RestOperations restOperations

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
      ResponseEntity<MetalArchivesReleasesResponse> responseEntity
      def today = LocalDate.now().toString()
      try {
        responseEntity = restOperations.getForEntity(UPCOMING_RELEASES_URL, MetalArchivesReleasesResponse, startOfRange, today)
      }
      catch (Exception e) {
        if (++attempt < MAX_ATTEMPTS) {
          log.info("Error during fetching releases (iDisplayStart=${startOfRange}). I will try again.")
          continue
        }
        else {
          log.error("5 errors in a row during fetching the releases. I give up.", e)
          break
        }
      }

      // (2) collect raw response data
      MetalArchivesReleasesResponse responseBody = responseEntity.body
      rawResponse.addAll(responseBody.data)

      // (3) prepare next iteration
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

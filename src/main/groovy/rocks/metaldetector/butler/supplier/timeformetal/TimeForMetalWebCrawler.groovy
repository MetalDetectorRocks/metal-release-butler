package rocks.metaldetector.butler.supplier.timeformetal

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

@Component
@Slf4j
class TimeForMetalWebCrawler {

  static final String UPCOMING_RELEASES_URL = "https://time-for-metal.eu/metal-releases-kalender/?pno={page}"

  @Autowired
  RestOperations restOperations

  String requestReleases(int page) {
    try {
      def responseEntity = restOperations.getForEntity(UPCOMING_RELEASES_URL, String, [page: page])
      return responseEntity.body
    }
    catch (Exception e) {
      log.error("Error during fetching releases for page ${page}", e)
      return null
    }
  }
}

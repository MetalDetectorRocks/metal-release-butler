package rocks.metaldetector.butler.supplier.timeformetal

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@Slf4j
class TimeForMetalWebCrawler {

  static final String UPCOMING_RELEASES_URL = "https://time-for-metal.eu/metal-releases-kalender/?pno={page}"

  @Autowired
  RestTemplate restTemplate

  String requestReleases(int page) {
    try {
      def responseEntity = restTemplate.getForEntity(UPCOMING_RELEASES_URL, String, [page: page])
      return responseEntity.body
    }
    catch (Exception e) {
      log.error("Error during fetching releases", e)
    }
  }
}

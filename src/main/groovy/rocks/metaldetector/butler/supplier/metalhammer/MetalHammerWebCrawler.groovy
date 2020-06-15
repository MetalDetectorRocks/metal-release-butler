package rocks.metaldetector.butler.supplier.metalhammer

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@Slf4j
class MetalHammerWebCrawler {

  static final String UPCOMING_RELEASES_URL = "https://www.metal-hammer.de/neue-metal-alben-kommende-veroeffentlichungen-1032003"

  @Autowired
  RestTemplate restTemplate

  String requestReleases() {
    try {
      def responseEntity = restTemplate.getForEntity(UPCOMING_RELEASES_URL, String)
      return responseEntity.body
    }
    catch (Exception e) {
      log.error("Error during fetching releases", e)
    }
  }
}

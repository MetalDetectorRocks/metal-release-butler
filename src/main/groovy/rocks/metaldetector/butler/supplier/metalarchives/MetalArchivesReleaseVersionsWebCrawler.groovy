package rocks.metaldetector.butler.supplier.metalarchives

import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component

@Slf4j
@Component
class MetalArchivesReleaseVersionsWebCrawler {

  static final String REST_ENDPOINT = "https://www.metal-archives.com/release/ajax-versions/current/releaseId/parent/releaseId"

  Document requestOtherReleases(String releaseId) {
    def url = REST_ENDPOINT.replaceAll("releaseId", releaseId)
    try {
      return Jsoup.connect(url).get()
    }
    catch (Exception e) {
      log.error("Error during fetching other releases page for metal archives release id '${releaseId}'", e)
    }
  }
}

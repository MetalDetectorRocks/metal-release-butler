package rocks.metaldetector.butler.supplier.metalarchives

import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Slf4j
@Component
class MetalArchivesReleaseVersionsWebCrawler implements MetalArchivesReleaseImportUtils {

  static final String REST_ENDPOINT = "https://www.metal-archives.com/release/ajax-versions/current/releaseId/parent/releaseId"

  @Value('${concurrency.throttling-in-seconds}')
  long throttlingInSeconds

  Document requestOtherReleases(String releaseId) {
    def url = REST_ENDPOINT.replaceAll("releaseId", releaseId)
    try {
      throttle(throttlingInSeconds)
      log.info("Request 'other releases' page -> $url")
      return Jsoup.connect(url).get()
    }
    catch (Exception e) {
      log.error("Error during fetching other releases page for metal archives release id '${releaseId}'", e)
      return null
    }
  }
}

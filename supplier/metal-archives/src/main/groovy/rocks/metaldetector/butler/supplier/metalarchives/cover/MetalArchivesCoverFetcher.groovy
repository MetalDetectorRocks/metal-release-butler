package rocks.metaldetector.butler.supplier.metalarchives.cover

import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.supplier.infrastructure.ReleaseEntityDeleteRequestEvent
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverFetcher
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesReleaseImportUtils

import java.util.concurrent.atomic.AtomicInteger

@Slf4j
@Service
class MetalArchivesCoverFetcher implements CoverFetcher, MetalArchivesReleaseImportUtils {

  static final String RELEASE_PAGE_NOT_FOUND_ERROR_MESSAGE = "status code: 404"
  static final String ALBUM_COVER_HTML_ID = "cover"
  static final int MAX_ATTEMPTS = 5
  AtomicInteger currentAttempt

  @Autowired
  ApplicationEventPublisher eventPublisher

  @Value('${concurrency.throttling-in-seconds}')
  long throttlingInSeconds

  @Override
  String fetchCoverUrl(String sourceUrl) {
    currentAttempt = new AtomicInteger(0)
    Document releasePage = fetchReleasePage(sourceUrl)
    Elements coverDivs = releasePage?.select("a")
        ?.findAll { it.id() == ALBUM_COVER_HTML_ID }
    Element coverDiv = coverDivs ? coverDivs.first() : null
    String coverLink = coverDiv?.attributes()?["href"]
    return coverLink
  }

  private Document fetchReleasePage(String sourceUrl) {
    throttle(throttlingInSeconds)
    try {
      currentAttempt.incrementAndGet()
      return Jsoup.connect(sourceUrl).get()
    }
    catch (Exception e) {
      return handleErrorAndTryAgain(e, sourceUrl)
    }
  }

  private Document handleErrorAndTryAgain(Exception exception, String sourceUrl) {
    if (exception.message?.containsIgnoreCase(RELEASE_PAGE_NOT_FOUND_ERROR_MESSAGE)) {
      log.info("The release page '${sourceUrl}' could not be found.")
      eventPublisher.publishEvent(new ReleaseEntityDeleteRequestEvent(this, sourceUrl))
      return null
    }
    else if (currentAttempt.intValue() < MAX_ATTEMPTS) {
      log.info("The release page '${sourceUrl}' could not be fetched. Reason: $exception.message. " +
               "Will wait some time and try again ($currentAttempt/$MAX_ATTEMPTS).")
      throttle(throttlingInSeconds)
      return fetchReleasePage(sourceUrl)
    }
    else {
      log.error("$MAX_ATTEMPTS errors in a row during fetching the release page. I give up.", exception)
      return null
    }
  }
}

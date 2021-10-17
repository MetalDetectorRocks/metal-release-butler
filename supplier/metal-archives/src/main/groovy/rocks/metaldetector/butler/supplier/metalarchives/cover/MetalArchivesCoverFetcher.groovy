package rocks.metaldetector.butler.supplier.metalarchives.cover

import groovy.util.logging.Slf4j
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.supplier.infrastructure.ReleaseEntityDeleteRequestEvent
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverFetcher

import java.util.concurrent.TimeUnit

@Slf4j
@Service
class MetalArchivesCoverFetcher implements CoverFetcher {

  static final String RELEASE_PAGE_NOT_FOUND_ERROR_MESSAGE = "status code: 404"
  static final String ALBUM_COVER_HTML_ID = "cover"
  static final int MAX_ATTEMPTS = 5
  int currentAttempt

  @Autowired
  ApplicationEventPublisher eventPublisher

  @Override
  String fetchCoverUrl(String sourceUrl) {
    currentAttempt = 0
    Document releasePage = fetchReleasePage(sourceUrl)
    Elements coverDivs = releasePage?.select("a")
        ?.findAll { it.id() == ALBUM_COVER_HTML_ID }
    Element coverDiv = coverDivs ? coverDivs.first() : null
    String coverLink = coverDiv?.attributes()?["href"]
    return coverLink
  }

  private Document fetchReleasePage(String sourceUrl) {
    try {
      currentAttempt++
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
    else if (currentAttempt < MAX_ATTEMPTS) {
      log.info("The release page '${sourceUrl}' could not be fetched. Reason: $exception.message. " +
               "I will wait 1 second and try again ($currentAttempt/$MAX_ATTEMPTS).")
      TimeUnit.SECONDS.sleep(1)
      return fetchReleasePage(sourceUrl)
    }
    else {
      log.error("5 errors in a row during fetching the release page. I give up.", exception)
      return null
    }
  }
}

package rocks.metaldetector.butler.supplier.metalarchives.cover

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.supplier.infrastructure.ReleaseEntityDeleteRequestEvent
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverFetcher
import rocks.metaldetector.butler.supplier.infrastructure.cover.HttpBuilderFunction

import java.util.concurrent.TimeUnit

@Slf4j
@Service
class MetalArchivesCoverFetcher implements CoverFetcher {

  static final String RELEASE_PAGE_NOT_FOUND_ERROR_MESSAGE = "status code: 404"
  static final String ALBUM_COVER_HTML_ID = "cover"
  static final int MAX_ATTEMPTS = 5
  int currentAttempt

  @Autowired
  HttpBuilderFunction httpBuilderFunction

  @Autowired
  ApplicationEventPublisher eventPublisher

  @Override
  String fetchCoverUrl(String sourceUrl) {
    currentAttempt = 0
    HttpBuilder httpBuilder = httpBuilderFunction.apply(sourceUrl)
    Document releasePage = fetchReleasePage(httpBuilder, sourceUrl)
    Elements coverDivs = releasePage?.select("a")
        ?.findAll { it.id() == ALBUM_COVER_HTML_ID }
    Element coverDiv = coverDivs ? coverDivs.first() : null
    String coverLink = coverDiv?.attributes()?["href"]
    return coverLink
  }

  private Document fetchReleasePage(HttpBuilder httpBuilder, String sourceUrl) {
    try {
      currentAttempt++
      return httpBuilder.get() as Document
    }
    catch (Exception e) {
      return handleErrorAndTryAgain(e, httpBuilder, sourceUrl)
    }
  }

  private Document handleErrorAndTryAgain(Exception exception, HttpBuilder httpBuilder, String sourceUrl) {
    if (exception.message?.containsIgnoreCase(RELEASE_PAGE_NOT_FOUND_ERROR_MESSAGE)) {
      log.info("The release page '${sourceUrl}' could not be found.")
      eventPublisher.publishEvent(new ReleaseEntityDeleteRequestEvent(this, sourceUrl))
      return null
    }
    else if (currentAttempt < MAX_ATTEMPTS) {
      log.info("The release page '${sourceUrl}' could not be fetched. Reason: $exception.message. " +
               "I will wait 1 second and try again ($currentAttempt/$MAX_ATTEMPTS).")
      TimeUnit.SECONDS.sleep(1)
      return fetchReleasePage(httpBuilder, sourceUrl)
    }
    else {
      log.error("5 errors in a row during fetching the release page. I give up.", exception)
      return null
    }
  }
}

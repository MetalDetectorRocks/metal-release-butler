package rocks.metaldetector.butler.service.cover

import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import rocks.metaldetector.butler.service.release.ReleaseEntityDeleteRequestEvent

import java.util.concurrent.TimeUnit

@Slf4j
@Service
class MetalArchivesCoverFetcher implements CoverFetcher {

  static final String RELEASE_PAGE_NOT_FOUND_ERROR_MESSAGE = "status code: 404"
  static final String ALBUM_COVER_HTML_ID = "cover"
  static final int MAX_ATTEMPTS = 5
  int currentAttempt

  @Autowired
  HTTPBuilderFunction httpBuilderFunction

  @Autowired
  ApplicationEventPublisher eventPublisher

  @Override
  String fetchCoverUrl(String sourceUrl) {
    currentAttempt = 0
    HTTPBuilder httpBuilder = httpBuilderFunction.apply(sourceUrl)
    def releasePage = fetchReleasePage(httpBuilder)
    def coverDivs = releasePage?."**"?.findAll { it.@id == ALBUM_COVER_HTML_ID }
    def coverDiv = coverDivs ? coverDivs.first() : null
    def coverLink = coverDiv?.@href?.text() as String
    return coverLink
  }

  private def fetchReleasePage(HTTPBuilder httpBuilder) {
    try {
      currentAttempt++
      return httpBuilder.get([:])
    }
    catch (Exception e) {
      return handleErrorAndTryAgain(e, httpBuilder)
    }
  }

  private def handleErrorAndTryAgain(Exception exception, HTTPBuilder httpBuilder) {
    if (exception.message?.containsIgnoreCase(RELEASE_PAGE_NOT_FOUND_ERROR_MESSAGE)) {
      log.info("The release page '${httpBuilder.uri}' could not be found.")
      eventPublisher.publishEvent(new ReleaseEntityDeleteRequestEvent(this, httpBuilder.uri.toString()))
      return null
    }
    else if (currentAttempt < MAX_ATTEMPTS) {
      log.info("The release page '${httpBuilder.uri}' could not be fetched. Reason: $exception.message. " +
               "I will wait 1 second and try again ($currentAttempt/$MAX_ATTEMPTS).")
      TimeUnit.SECONDS.sleep(1)
      return fetchReleasePage(httpBuilder)
    }
    else {
      log.error("5 errors in a row during fetching the release page. I give up.", exception)
      return null
    }
  }
}

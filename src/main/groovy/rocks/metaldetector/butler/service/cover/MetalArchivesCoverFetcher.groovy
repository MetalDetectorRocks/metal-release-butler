package rocks.metaldetector.butler.service.cover

import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import org.springframework.stereotype.Service

import java.util.concurrent.TimeUnit

@Slf4j
@Service
class MetalArchivesCoverFetcher implements CoverFetcher {

  static final String ALBUM_COVER_HTML_ID = "cover"
  static final int MAX_ATTEMPTS = 5
  int currentAttempt

  @Override
  URL fetchCoverUrl(URL metalArchivesAlbumUrl) {
    currentAttempt = 0
    HTTPBuilder httpBuilder = new HTTPBuilder(metalArchivesAlbumUrl)
    def releasePage = fetchReleasePage(httpBuilder)
    def coverDiv = releasePage?."**"?.findAll { it.@id == ALBUM_COVER_HTML_ID }?.first()
    def coverLink = coverDiv?.@href?.text() as String
    return coverLink ? new URL(coverLink) : null
  }

  def fetchReleasePage(HTTPBuilder httpBuilder) {
    try {
      currentAttempt++
      return httpBuilder.get([:])
    }
    catch (Exception e) {
      return handleErrorAndTryAgain(e, httpBuilder)
    }
  }

  private def handleErrorAndTryAgain(Exception e, HTTPBuilder httpBuilder) {
    if (currentAttempt < MAX_ATTEMPTS) {
      log.warn("Error during fetching the release page '${httpBuilder.uri}' (${e.message}). I will wait 1 second and try again.")
      TimeUnit.SECONDS.sleep(1)
      return fetchReleasePage(httpBuilder)
    }
    else {
      log.error("5 errors in a row during fetching the release page. I give up.", e)
      return null
    }
  }
}

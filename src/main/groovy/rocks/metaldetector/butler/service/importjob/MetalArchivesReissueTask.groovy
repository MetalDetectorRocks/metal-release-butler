package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesWebCrawler

class MetalArchivesReissueTask implements Runnable {

  ReleaseEntity releaseEntity
  MetalArchivesWebCrawler webCrawler = new MetalArchivesWebCrawler()

  @Override
  void run() {
    def urlParts = releaseEntity.releaseDetailsUrl?.split("/")
    if (urlParts?.size() > 1) {
      def releaseId = urlParts[urlParts.length - 1]
      def document = webCrawler.requestOtherReleases(releaseId)
      Set<String> distinctDateRows = document?.getElementsByTag("a")?.drop(1)
                                         ?.collect { it?.childNode(0)?.value as String } - null
      releaseEntity.reissue = distinctDateRows.size() > 1
    }
  }
}

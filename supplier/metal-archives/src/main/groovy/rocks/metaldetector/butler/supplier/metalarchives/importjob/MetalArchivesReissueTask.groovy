package rocks.metaldetector.butler.supplier.metalarchives.importjob

import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.supplier.metalarchives.MetalArchivesReleaseVersionsWebCrawler

class MetalArchivesReissueTask implements Runnable {

  ReleaseEntity releaseEntity
  MetalArchivesReleaseVersionsWebCrawler webCrawler

  @Override
  void run() {
    def urlParts = releaseEntity.releaseDetailsUrl?.split("/")
    if (urlParts?.size() > 1) {
      def releaseId = urlParts[urlParts.length - 1]
      def document = webCrawler.requestOtherReleases(releaseId)
      Set<String> distinctDateRows = document?.getElementsByTag("a")?.drop(1)
                                         ?.collect { it?.childNode(0)?.value as String }?.findAll()
      releaseEntity.reissue = distinctDateRows?.size() > 1
    }
  }
}

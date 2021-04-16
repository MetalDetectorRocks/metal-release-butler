package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.service.cover.CoverService

class CoverTransferTask implements Runnable {

  ReleaseEntity releaseEntity
  CoverService coverService

  @Override
  void run() {
    def targetFolder = composeTargetFolderName()
    String transferTargetAddress = coverService.transfer(releaseEntity.releaseDetailsUrl, targetFolder)
    releaseEntity.coverUrl = transferTargetAddress
  }

  private String composeTargetFolderName() {
    def releaseDate = releaseEntity.releaseDate
    return releaseDate ? "${releaseDate.getYear()}/${releaseDate.getMonthValue()}" : releaseEntity.estimatedReleaseDate
  }
}

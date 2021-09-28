package rocks.metaldetector.butler.supplier.infrastructure.importjob

import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService

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

package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.service.cover.CoverService

class CoverTransferTask implements Runnable {

  ReleaseEntity releaseEntity
  CoverService coverService

  @Override
  void run() {
    String transferTargetAddress = coverService.transfer(releaseEntity.releaseDetailsUrl)
    releaseEntity.setCoverUrl(transferTargetAddress)
  }
}

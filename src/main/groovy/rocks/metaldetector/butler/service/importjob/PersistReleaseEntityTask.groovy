package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.cover.CoverService

class PersistReleaseEntityTask implements Runnable {

  ReleaseEntity releaseEntity
  CoverService coverService
  ReleaseRepository releaseRepository

  @Override
  void run() {
    if (coverService) {
      String transferTargetAddress = coverService.transfer(releaseEntity.releaseDetailsUrl)
      releaseEntity.setCoverUrl(transferTargetAddress)
    }
    releaseRepository.save(releaseEntity)
  }
}

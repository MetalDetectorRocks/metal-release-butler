package rocks.metaldetector.butler.service.release

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseRepository
import rocks.metaldetector.butler.service.cover.CoverService

class PersistReleaseEntityTask implements Runnable {

  private ReleaseEntity releaseEntity
  private CoverService coverService
  private ReleaseRepository releaseRepository

  @Override
  void run() {
    String transferTargetAddress = coverService.transfer(releaseEntity.metalArchivesAlbumUrl)
    releaseEntity.setCoverUrl(transferTargetAddress)
    releaseRepository.save(releaseEntity)
  }
}

package rocks.metaldetector.butler.service

import rocks.metaldetector.butler.model.release.ReleaseEntity

interface CoverService {

  void downloadReleaseCover(ReleaseEntity release)

}
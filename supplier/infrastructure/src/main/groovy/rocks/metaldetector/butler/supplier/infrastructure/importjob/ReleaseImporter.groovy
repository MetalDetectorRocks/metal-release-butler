package rocks.metaldetector.butler.supplier.infrastructure.importjob

import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource
import rocks.metaldetector.butler.supplier.infrastructure.cover.CoverService

interface ReleaseImporter {

  ImportResult importReleases()

  ReleaseSource getReleaseSource()

  void retryCoverDownload()

  CoverService getCoverService()

}

package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.service.cover.CoverService

interface ReleaseImporter {

  ImportResult importReleases()

  ReleaseSource getReleaseSource()

  void retryCoverDownload()

  CoverService getCoverService()

}
package rocks.metaldetector.butler.service.importjob

import rocks.metaldetector.butler.model.release.ReleaseSource

interface ReleaseImporter {

  ImportResult importReleases()

  ReleaseSource getReleaseSource()

}

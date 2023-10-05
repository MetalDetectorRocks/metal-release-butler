package rocks.metaldetector.butler.service.importjob

import groovy.util.logging.Slf4j
import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobEntity
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ImportResult
import rocks.metaldetector.butler.supplier.infrastructure.importjob.ReleaseImporter

import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.ERROR
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.RUNNING
import static rocks.metaldetector.butler.persistence.domain.importjob.JobState.SUCCESSFUL

@Slf4j
class ImportJobTask implements Runnable {

  ReleaseImporter releaseImporter
  ImportJobService importJobService
  ImportJobEntity importJob

  @Override
  void run() {
    importJobService.updateImportJob(importJob, RUNNING)
    ImportResult importResult = null
    try {
      importResult = releaseImporter.importReleases()
    }
    catch (Exception e) {
      log.error("Error during import of releases from '${releaseImporter.getReleaseSource()?.displayName}'", e)
      importJobService.updateImportJob(importJob, ERROR)
    }
    if (importResult) {
      importJobService.updateImportJob(importJob, SUCCESSFUL, importResult)
    }
  }
}

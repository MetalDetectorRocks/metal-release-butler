package rocks.metaldetector.butler.service.importjob

interface ReleaseImporterWithCover extends ReleaseImporter {

  void retryCoverDownload()
}
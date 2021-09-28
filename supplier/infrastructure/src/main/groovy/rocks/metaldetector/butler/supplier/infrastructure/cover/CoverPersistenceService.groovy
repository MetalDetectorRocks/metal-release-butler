package rocks.metaldetector.butler.supplier.infrastructure.cover

interface CoverPersistenceService {

  String persistCover(URL coverUrl, String targetFolder)

}

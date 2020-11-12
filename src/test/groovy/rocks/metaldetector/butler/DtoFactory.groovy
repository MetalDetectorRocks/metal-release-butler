package rocks.metaldetector.butler

import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseEntityState
import rocks.metaldetector.butler.web.dto.ReleaseDto

import java.time.LocalDate
import java.time.LocalDateTime

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH

class DtoFactory {

  static class ReleaseEntityFactory {

    static ReleaseEntity createReleaseEntity(String artist) {
      return createReleaseEntity(artist, LocalDate.now())
    }

    static ReleaseEntity createReleaseEntity(String artist, LocalDate releaseDate) {
      return new ReleaseEntity(
          id: 1L,
          artist: artist,
          albumTitle: "T",
          releaseDate: releaseDate,
          state: ReleaseEntityState.OK,
          estimatedReleaseDate: "releaseDate",
          additionalArtists: artist,
          source: METAL_ARCHIVES,
          genre: "genre",
          type: FULL_LENGTH,
          metalArchivesAlbumUrl: new URL("http://www.internet.de"),
          metalArchivesArtistUrl: new URL("http://www.internet2.de"),
          coverUrl: "coverUrl"
      )
    }
  }

  static class ImportJobEntityFactory {

    static ImportJobEntity createImportJobEntity() {
      return new ImportJobEntity(
          jobId: UUID.randomUUID(),
          totalCountRequested: 666,
          totalCountImported: 666,
          startTime: LocalDateTime.now(),
          endTime: LocalDateTime.now(),
          source: METAL_ARCHIVES
      )
    }
  }

  static class ReleaseDtoFactory {

    static ReleaseDto createReleaseDto(String artist) {
      return createReleaseDto(artist, LocalDate.now())
    }

    static ReleaseDto createReleaseDto(String artist, LocalDate releaseDate) {
      return new ReleaseDto(
          artist: artist,
          additionalArtists: [artist],
          albumTitle: "T",
          releaseDate: releaseDate,
          estimatedReleaseDate: "releaseDate",
          state: ReleaseEntityState.OK,
          source: METAL_ARCHIVES,
          genre: "genre",
          type: FULL_LENGTH,
          metalArchivesAlbumUrl: new URL("http://www.internet.de").toExternalForm(),
          metalArchivesArtistUrl: new URL("http://www.internet2.de").toExternalForm(),
          coverUrl: "coverUrl"
      )
    }
  }
}

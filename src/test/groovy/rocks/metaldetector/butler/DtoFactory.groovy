package rocks.metaldetector.butler

import rocks.metaldetector.butler.model.importjob.ImportJobEntity
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.web.dto.ReleaseDto

import java.time.LocalDate
import java.time.LocalDateTime

import static rocks.metaldetector.butler.model.release.ReleaseEntityState.OK
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseSource.TEST
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH

class DtoFactory {

  static class ReleaseEntityFactory {

    static ReleaseEntity createReleaseEntity(String artist) {
      return createReleaseEntity(1L, artist, LocalDate.now())
    }

    static ReleaseEntity createReleaseEntity(String artist, LocalDate releaseDate) {
      return createReleaseEntity(1L, artist, releaseDate)
    }

    static ReleaseEntity createReleaseEntity(Long id, String artist, LocalDate releaseDate) {
      return new ReleaseEntity(
          id: id,
          artist: artist,
          albumTitle: "T",
          releaseDate: releaseDate,
          state: OK,
          estimatedReleaseDate: "releaseDate",
          additionalArtists: artist,
          source: TEST,
          genre: "genre",
          type: FULL_LENGTH,
          releaseDetailsUrl: "http://www.internet.de",
          artistDetailsUrl: "http://www.internet2.de",
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
          state: OK,
          source: METAL_ARCHIVES,
          genre: "genre",
          type: FULL_LENGTH,
          releaseDetailsUrl: "http://www.internet.de",
          artistDetailsUrl: new URL("http://www.internet2.de").toExternalForm(),
          coverUrl: "coverUrl"
      )
    }
  }
}

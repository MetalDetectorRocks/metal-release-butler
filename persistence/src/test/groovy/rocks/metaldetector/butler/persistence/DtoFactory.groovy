package rocks.metaldetector.butler.persistence

import rocks.metaldetector.butler.persistence.domain.importjob.ImportJobEntity
import rocks.metaldetector.butler.persistence.domain.importjob.JobState
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource

import java.time.LocalDate
import java.time.LocalDateTime

import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.OK
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TEST
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseType.FULL_LENGTH

class DtoFactory {

  static class ReleaseEntityFactory {

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
          coverUrl: "coverUrl",
          createdDateTime: new Date()
      )
    }
  }

  static class ImportJobEntityFactory {

    static ImportJobEntity createImportJobEntity(ReleaseSource releaseSource, JobState state, LocalDateTime startTime, LocalDateTime endTime) {
      return new ImportJobEntity(
          jobId: UUID.randomUUID(),
          source: releaseSource,
          state: state,
          startTime: startTime,
          endTime: endTime
      )
    }
  }
}

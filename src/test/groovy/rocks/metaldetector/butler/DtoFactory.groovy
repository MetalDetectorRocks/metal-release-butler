package rocks.metaldetector.butler

import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseEntityRecordState
import rocks.metaldetector.butler.web.dto.ReleaseDto

import java.time.LocalDate

class DtoFactory {

  static class ReleaseEntityFactory {

    static ReleaseEntity createReleaseEntity(String artist, LocalDate releaseDate) {
      return new ReleaseEntity(artist: artist, albumTitle: "T", releaseDate: releaseDate, state: ReleaseEntityRecordState.OK,
                               estimatedReleaseDate: "releaseDate", additionalArtists: artist)
    }
  }

  static class ReleaseDtoFactory {

    static ReleaseDto createReleaseDto(String artist, LocalDate releaseDate) {
      return new ReleaseDto(artist: artist, additionalArtists: [artist], albumTitle: "T",
                            releaseDate: releaseDate, estimatedReleaseDate: "releaseDate",
                            state: ReleaseEntityRecordState.OK)
    }
  }
}

package com.metalr2.butler

import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseEntityRecordState
import com.metalr2.butler.web.dto.ReleaseDto

import java.time.LocalDate

class DtoFactory {

  static class ReleaseEntityFactory {

    static ReleaseEntity one(String artist, LocalDate releaseDate) {
      return new ReleaseEntity(artist: artist, albumTitle: "T", releaseDate: releaseDate, state: ReleaseEntityRecordState.OK,
                               estimatedReleaseDate: "releaseDate", additionalArtists: artist)
    }

    static List<ReleaseEntity> multiple(int number, LocalDate releaseDate) {
      def entities = [];
      for (def i = 0; i < number; i++) {
        entities.add(new ReleaseEntity(artist: "A" + i, albumTitle: "T", releaseDate: releaseDate, state: ReleaseEntityRecordState.OK,
                                       estimatedReleaseDate: "releaseDate", additionalArtists: "A" + i))
      }
      return entities
    }
  }

  static class ReleaseDtoFactory {

    static ReleaseDto one(String artist, LocalDate releaseDate) {
      return new ReleaseDto(artist, [artist], "T", releaseDate, "releaseDate")
    }

    static List<ReleaseDto> multiple(int number, LocalDate releaseDate) {
      def dtos = [];
      for (def i = 0; i < number; i++) {
        dtos.add(new ReleaseDto("A" + i, ["A" + i], "T", releaseDate, "releaseDate"))
      }
      return dtos
    }
  }
}

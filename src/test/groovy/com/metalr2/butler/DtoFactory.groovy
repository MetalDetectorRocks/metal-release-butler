package com.metalr2.butler

import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseEntityRecordState
import com.metalr2.butler.web.dto.ReleaseDto

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
      return new ReleaseDto(artist, [artist], "T", releaseDate, "releaseDate")
    }
  }
}

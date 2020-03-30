package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical
import rocks.metaldetector.butler.model.release.ReleaseEntityRecordState
import rocks.metaldetector.butler.model.release.ReleaseSource
import rocks.metaldetector.butler.model.release.ReleaseType

import java.time.LocalDate

@Canonical
class ReleaseDto {

  String artist
  List<String> additionalArtists = []
  String albumTitle
  LocalDate releaseDate
  String estimatedReleaseDate
  String genre
  ReleaseType type
  String metalArchivesArtistUrl
  String metalArchivesAlbumUrl
  ReleaseSource source
  ReleaseEntityRecordState state

}

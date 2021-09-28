package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical
import groovy.transform.builder.Builder
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState
import rocks.metaldetector.butler.persistence.domain.release.ReleaseSource
import rocks.metaldetector.butler.persistence.domain.release.ReleaseType

import java.time.LocalDate

@Canonical
@Builder
class ReleaseDto {

  Long id
  String artist
  List<String> additionalArtists = []
  String albumTitle
  LocalDate releaseDate
  LocalDate announcementDate
  String estimatedReleaseDate
  String genre
  ReleaseType type
  String artistDetailsUrl
  String releaseDetailsUrl
  ReleaseSource source
  ReleaseEntityState state
  String coverUrl
  Boolean reissue

}

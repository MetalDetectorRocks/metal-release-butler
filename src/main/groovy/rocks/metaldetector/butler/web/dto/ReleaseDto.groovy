package rocks.metaldetector.butler.web.dto

import groovy.transform.Canonical

import java.time.LocalDate

@Canonical
class ReleaseDto {

  String artist
  List<String> additionalArtists = []
  String albumTitle
  LocalDate releaseDate
  String estimatedReleaseDate

}

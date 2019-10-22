package com.metalr2.butler.web.dto

import groovy.transform.Canonical

import java.time.LocalDate

@Canonical
class ReleaseDto {

  String artist
  URL artistUrl
  List<String> additionalArtists = []
  String albumTitle
  URL albumUrl
  String type
  String genre
  LocalDate releaseDate

  // for inaccurate data such as "Summer 2020"
  String estimatedReleaseDate

}

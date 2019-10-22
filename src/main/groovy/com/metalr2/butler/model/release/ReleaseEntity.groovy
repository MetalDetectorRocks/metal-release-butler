package com.metalr2.butler.model.release

import com.metalr2.butler.model.BaseEntity
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import java.time.LocalDate

@Entity(name = "releases")
@EqualsAndHashCode(callSuper = true)
@Builder(excludes = "new") // because there is method isNew in super class
class ReleaseEntity extends BaseEntity {

  @Column(name = "artist", nullable = false)
  String artist

  @Column(name = "album_title", nullable = false)
  String albumTitle

  @Column(name = "release_date", nullable = true)
  LocalDate releaseDate

  @Column(name = "estimated_release_date", nullable = true)
  String estimatedReleaseDate

  @Column(name = "genre", nullable = true)
  String genre

  @Column(name = "type", nullable = true)
  @Enumerated(EnumType.STRING)
  ReleaseType type

  @Column(name = "metal_archives_artist_url", nullable = true)
  URL metalArchivesArtistUrl

  @Column(name = "metal_archives_album_url", nullable = true)
  URL metalArchivesAlbumUrl

  @Column(name = "release_source", nullable = true)
  @Enumerated(EnumType.STRING)
  // for inaccurate data such as "Summer 2020"
  ReleaseSource releaseSource

  @Column(name = "additional_artists", nullable = true)
  String additionalArtists

  @Column(name = "state", nullable = false)
  @Enumerated(EnumType.STRING)
  ReleaseEntityRecordState state

  List<String> getAdditionalArtists() {
    return additionalArtists ? additionalArtists.split(", ")?.toList() : []
  }

}

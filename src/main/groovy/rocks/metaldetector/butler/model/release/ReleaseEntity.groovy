package rocks.metaldetector.butler.model.release

import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder
import rocks.metaldetector.butler.model.BaseEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import java.time.LocalDate

@Entity(name = "releases")
@EqualsAndHashCode(callSuper = false)
@Builder(excludes = "new") // because there is method isNew in super class
class ReleaseEntity extends BaseEntity implements Comparable<ReleaseEntity> {

  @Column(name = "artist", nullable = false, columnDefinition = "varchar(255)")
  String artist

  @Column(name = "album_title", nullable = false, columnDefinition = "varchar(255)")
  String albumTitle

  @Column(name = "release_date", nullable = true, columnDefinition = "date")
  LocalDate releaseDate

  @Column(name = "estimated_release_date", nullable = true)
  String estimatedReleaseDate

  @Column(name = "genre", nullable = true)
  String genre

  @Column(name = "type", nullable = true)
  @Enumerated(EnumType.STRING)
  ReleaseType type

  @Column(name = "metal_archives_artist_url", nullable = true, length = 500)
  URL metalArchivesArtistUrl

  @Column(name = "metal_archives_album_url", nullable = true, length = 500)
  URL metalArchivesAlbumUrl

  @Column(name = "source", nullable = true)
  @Enumerated(EnumType.STRING)
  ReleaseSource source

  @Column(name = "additional_artists", nullable = true, columnDefinition = "varchar(255)")
  String additionalArtists

  @Column(name = "state", nullable = false)
  @Enumerated(EnumType.STRING)
  ReleaseEntityRecordState state

  @Column(name = "cover_url", nullable = true)
  String coverUrl

  List<String> getAdditionalArtists() {
    return additionalArtists ? additionalArtists.tokenize(",")*.trim() : []
  }

  @Override
  int compareTo(ReleaseEntity other) {
    // method is used for sorting and removing duplicates
    return this.releaseDate <=> other.releaseDate ?: this.artist <=> other.artist ?: this.albumTitle <=> other.albumTitle
  }

}

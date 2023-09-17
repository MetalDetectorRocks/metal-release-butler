package rocks.metaldetector.butler.persistence.domain.release

import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import rocks.metaldetector.butler.persistence.domain.BaseEntity

import java.time.LocalDate

@Entity(name = "releases")
@EqualsAndHashCode(callSuper = false)
@Builder(excludes = "new") // because there is method isNew in super class
class ReleaseEntity extends BaseEntity implements Comparable<ReleaseEntity> {

  @Column(name = "artist", nullable = false)
  String artist

  @Column(name = "album_title", nullable = false)
  String albumTitle

  @Column(name = "release_date")
  LocalDate releaseDate

  @Column(name = "estimated_release_date", nullable = true)
  String estimatedReleaseDate

  @Column(name = "genre", nullable = true)
  String genre

  @Column(name = "type", nullable = true)
  @Enumerated(EnumType.STRING)
  ReleaseType type

  @Column(name = "artist_details_url", length = 1000)
  String artistDetailsUrl

  @Column(name = "release_details_url", length = 1000)
  String releaseDetailsUrl

  @Column(name = "source")
  @Enumerated(EnumType.STRING)
  ReleaseSource source

  @Column(name = "additional_artists")
  String additionalArtists

  @Column(name = "state", nullable = false)
  @Enumerated(EnumType.STRING)
  ReleaseEntityState state

  @Column(name = "cover_url", length = 1000)
  String coverUrl

  @Column(name = "reissue")
  boolean reissue = false

  List<String> getAdditionalArtists() {
    return additionalArtists ? additionalArtists.tokenize(",")*.trim() : []
  }

  @Override
  int compareTo(ReleaseEntity other) {
    // method is used for sorting and removing duplicates
    return this.releaseDate <=> other.releaseDate ?: this.artist <=> other.artist ?: this.albumTitle <=> other.albumTitle
  }
}

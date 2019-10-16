package com.metalr2.butler.model.release

import com.metalr2.butler.model.BaseEntity
import groovy.transform.EqualsAndHashCode

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity(name = "release_announcements")
@EqualsAndHashCode(callSuper = true)
class ReleaseAnnouncement extends BaseEntity {

  @Column(name = "artist", nullable = false)
  String artist

  @Column(name = "album_title", nullable = false)
  String albumTitle

  @Column(name = "release_date", nullable = false)
  String releaseTime

  @Column(name = "genre", nullable = true)
  String genre

  @Column(name = "type", nullable = true)
  @Enumerated(EnumType.STRING)
  ReleaseType type

}

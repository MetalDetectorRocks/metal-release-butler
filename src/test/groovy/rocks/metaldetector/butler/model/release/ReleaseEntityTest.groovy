package rocks.metaldetector.butler.model.release

import spock.lang.Specification

import java.time.LocalDate

class ReleaseEntityTest extends Specification {

  def "should be sort by release date at first"() {
    given:
    def release1 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 1),
            artist: "C",
            albumTitle: "C"
    )
    def release2 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 2),
            artist: "B",
            albumTitle: "B"
    )
    def release3 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 3),
            artist: "A",
            albumTitle: "A"
    )
    def releaseEntities = [release3, release1, release2]

    when:
    Collections.sort(releaseEntities)

    then:
    releaseEntities == [release1, release2, release3]
  }

  def "should be sort by artist at second"() {
    given:
    def release1 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 1),
            artist: "A",
            albumTitle: "C"
    )
    def release2 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 1),
            artist: "B",
            albumTitle: "A"
    )
    def release3 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 1),
            artist: "C",
            albumTitle: "A"
    )
    def releaseEntities = [release3, release1, release2]

    when:
    Collections.sort(releaseEntities)

    then:
    releaseEntities == [release1, release2, release3]
  }

  def "should be sort by album title as third"() {
    given:
    def release1 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 1),
            artist: "A",
            albumTitle: "A"
    )
    def release2 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 1),
            artist: "A",
            albumTitle: "B"
    )
    def release3 = new ReleaseEntity(
            releaseDate: LocalDate.of(2020, 6, 1),
            artist: "A",
            albumTitle: "C"
    )
    def releaseEntities = [release3, release1, release2]

    when:
    Collections.sort(releaseEntities)

    then:
    releaseEntities == [release1, release2, release3]
  }
}

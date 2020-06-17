package rocks.metaldetector.butler.service.converter

import org.springframework.core.io.ClassPathResource
import rocks.metaldetector.butler.model.release.ReleaseEntity
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter.AUTUMN_ENGLISH
import static rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter.AUTUMN_GERMAN
import static rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter.SPRING_ENGLISH
import static rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter.SUMMER_ENGLISH
import static rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter.SUMMER_GERMAN
import static rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter.WINTER_ENGLISH
import static rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter.WINTER_GERMAN
import static rocks.metaldetector.butler.service.converter.MetalHammerReleaseEntityConverter.getSPRING_GERMAN

class MetalHammerReleaseEntityConverterTest extends Specification {

  MetalHammerReleaseEntityConverter underTest = new MetalHammerReleaseEntityConverter()
  def sourceString = new ClassPathResource("mock-releases-page-metal-hammer.txt").inputStream.text

  def "Should convert raw data into release entities"() {
    when:
    def result = underTest.convert(sourceString)

    then:
    result.size() == 2

    and:
    result[0] == new ReleaseEntity(artist: "Darkthrone",
                                   albumTitle: "Transilvanian Hunger",
                                   releaseDate: LocalDate.of(LocalDate.now().year, 6, 6))

    and:
    result[1] == new ReleaseEntity(artist: "Mayhem",
                                   albumTitle: "De Mysteriis Dom Sathanas",
                                   releaseDate: LocalDate.of(LocalDate.now().year, 7, 7))
  }

  @Unroll
  "Artist names are corrected"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()

    when:
    underTest.setArtist(releaseEntityBuilder, artist)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.artist == expectedArtist

    where:
    artist            | expectedArtist
    "Mayhem"          | "Mayhem"
    "Von Till, Steve" | "Steve Von Till"
  }

  @Unroll
  "Album titles are capitalized correctly"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()

    when:
    underTest.setAlbumTitle(releaseEntityBuilder, albumTitle)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.albumTitle == expectedAlbumTitle

    where:
    albumTitle                   | expectedAlbumTitle
    "FOR THE LOVE OF METAL LIVE" | "For The Love Of Metal Live"
    "fOR tHE lOVE oF mETAL lIVE" | "For The Love Of Metal Live"
    "for the love of metal live" | "For The Love Of Metal Live"
  }

  def "Current year is set for release date if nothing else is provided"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()
    def dateString = "6.6."

    when:
    underTest.setDate(releaseEntityBuilder, dateString)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.releaseDate == LocalDate.of(LocalDate.now().year, 6, 6)
  }

  def "Correct year is set for release date if provided"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()
    def dateString = "6.6.2020"

    when:
    underTest.setDate(releaseEntityBuilder, dateString)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.releaseDate == LocalDate.of(2020, 6, 6)
  }

  def "If only a year is given estimated release date is set"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()
    def dateString = "2020"

    when:
    underTest.setDate(releaseEntityBuilder, dateString)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.estimatedReleaseDate == dateString
  }

  @Unroll
  "If season '#dateString' is given estimated release date '#expectedDateString' is set"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()

    when:
    underTest.setDate(releaseEntityBuilder, dateString)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.estimatedReleaseDate == expectedDateString

    where:
    dateString    | expectedDateString
    SPRING_GERMAN | SPRING_ENGLISH
    SUMMER_GERMAN | SUMMER_ENGLISH
    AUTUMN_GERMAN | AUTUMN_ENGLISH
    WINTER_GERMAN | WINTER_ENGLISH
  }
}

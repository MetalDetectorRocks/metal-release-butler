package rocks.metaldetector.butler.service.converter

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChild
import org.springframework.core.io.ClassPathResource
import rocks.metaldetector.butler.model.release.ReleaseEntity
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseEntityState.*
import static rocks.metaldetector.butler.model.release.ReleaseSource.*
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH
import static rocks.metaldetector.butler.model.release.ReleaseType.EP

class TimeForMetalReleaseEntityConverterTest extends Specification {

  TimeForMetalReleaseEntityConverter underTest = new TimeForMetalReleaseEntityConverter(xmlSlurper: Spy(XmlSlurper))
  def sourceString = new ClassPathResource("mock-releases-page-time-for-metal.txt").inputStream.text
  static final String LONG_DASH = "–"

  def "Should convert raw data into release entities"() {
    when:
    def result = underTest.convert(sourceString)

    then:
    result.size() == 2

    and:
    result[0] == new ReleaseEntity(artist: "Artist 1",
                                   albumTitle: "Album 1",
                                   releaseDate: LocalDate.of(2020, 1, 1),
                                   type: FULL_LENGTH)
    result[0].releaseDetailsUrl == "http://cover1.com/album.jpg"
    result[0].type == FULL_LENGTH
    result[0].source == TIME_FOR_METAL
    result[0].state == OK

    and:
    result[1] == new ReleaseEntity(artist: "Artist 2",
                                   albumTitle: "Album 2",
                                   releaseDate: LocalDate.of(2020, 2, 1),
                                   type: FULL_LENGTH)
    result[1].releaseDetailsUrl == null
    result[1].type == FULL_LENGTH
    result[1].source == TIME_FOR_METAL
    result[1].state == OK

    and:
    2 * underTest.xmlSlurper.parseText(*_)
  }

  @Unroll
  "artist name is set"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()

    when:
    underTest.setArtistName(releaseEntityBuilder, rawValue)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.artist == "Artist 1"

    where:
    rawValue << [
        "Artist 1 - Album",
        "     Artist 1     -    Album",
        "Artist 1 $LONG_DASH Album"
    ]
  }

  @Unroll
  "album title is set correctly"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()

    when:
    underTest.setAlbumTitle(releaseEntityBuilder, title)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.albumTitle == expectedTitle

    where:
    title                                       | expectedTitle
    "Artist 1 - Album 1"                        | "Album 1"
    "Artist 1 -      Album 1        "           | "Album 1"
    "Artist 1 - Album 1 (EP)"                   | "Album 1"
    "Artist 1 - Album 1 - Live - 1964"          | "Album 1 - Live - 1964"
    "Artist 1 $LONG_DASH Album 1 - Live - 1964" | "Album 1 - Live - 1964"
  }

  def "release date is set"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()

    when:
    underTest.setReleaseDate(releaseEntityBuilder, "24.12.2020")

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.releaseDate == LocalDate.of(2020, 12, 24)
  }

  def "should set release type"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()

    when:
    underTest.setReleaseType(releaseEntityBuilder, rawValue)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.type == expectedReleaseType

    where:
    rawValue                  | expectedReleaseType
    "Artist - Album"          | FULL_LENGTH
    "Artist - Album (Single)" | FULL_LENGTH
    "Artist - Album (EP)"     | EP
  }

  def "cover source url is set"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()
    def sourceUrl = "sourceUrl"
    def nodeChildMock = Mock(NodeChild)
    nodeChildMock.attributes() >> [src: sourceUrl]

    when:
    underTest.setReleaseDetailsUrl(releaseEntityBuilder, nodeChildMock)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.releaseDetailsUrl == sourceUrl
  }
}

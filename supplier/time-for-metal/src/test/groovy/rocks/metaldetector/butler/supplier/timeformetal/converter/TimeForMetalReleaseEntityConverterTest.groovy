package rocks.metaldetector.butler.supplier.timeformetal.converter

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChild
import org.ccil.cowan.tagsoup.Parser
import org.springframework.core.io.ClassPathResource
import rocks.metaldetector.butler.persistence.domain.release.ReleaseEntity
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.persistence.domain.release.ReleaseEntityState.OK
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseSource.TIME_FOR_METAL
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseType.EP
import static rocks.metaldetector.butler.persistence.domain.release.ReleaseType.FULL_LENGTH

class TimeForMetalReleaseEntityConverterTest extends Specification {

  def xmlSlurper = new XmlSlurper(new Parser())
  TimeForMetalReleaseEntityConverter underTest = new TimeForMetalReleaseEntityConverter(timeForMetalXmlSlurper: Spy(xmlSlurper))

  def sourceString = new ClassPathResource("mock-releases-page-time-for-metal.txt").inputStream.text
  static final String LONG_DASH = "–"

  def "Should convert raw data into release entities"() {
    when:
    def result = underTest.convert(sourceString)

    then:
    result.size() == 3

    and:
    result[0] == new ReleaseEntity(artist: "Artist 1",
                                   albumTitle: "Album 1",
                                   releaseDate: LocalDate.of(2023, 3, 9))
    result[0].releaseDetailsUrl == "https://cover1.com/album.jpg"
    result[0].genre == "Death Metal"
    result[0].type == FULL_LENGTH
    result[0].source == TIME_FOR_METAL
    result[0].state == OK

    and:
    result[1] == new ReleaseEntity(artist: "Artist 2",
                                   albumTitle: "Album 2",
                                   releaseDate: LocalDate.of(2023, 3, 10))
    result[1].releaseDetailsUrl == "https://cover2.com/album.jpg"
    result[1].genre == "Black Metal"
    result[1].type == FULL_LENGTH
    result[1].source == TIME_FOR_METAL
    result[1].state == OK

    and:
    result[2] == new ReleaseEntity(artist: "Artist 3",
                                   albumTitle: "Album 3",
                                   releaseDate: LocalDate.of(2023, 3, 19))
    result[2].releaseDetailsUrl == "https://cover3.com/album.jpg"
    result[2].genre == "Death Metal"
    result[2].type == EP
    result[2].source == TIME_FOR_METAL
    result[2].state == OK

    and:
    2 * underTest.timeForMetalXmlSlurper.parseText(*_)
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
    "Artist-1 - Album 1"                        | "Album 1"
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

  @Unroll
  def "genre is set"() {
    given:
    def releaseEntityBuilder = ReleaseEntity.builder()
    def nodeChildMock = Mock(NodeChild)
    nodeChildMock.localText() >> [rawValue]

    when:
    underTest.setGenre(releaseEntityBuilder, nodeChildMock)

    then:
    def releaseEntity = releaseEntityBuilder.build()
    releaseEntity.genre == expectedGenre

    where:
    rawValue           | expectedGenre
    "Black Metal"      | "Black Metal"
    "Black Metal   "   | "Black Metal"
    "Black Metal\n"    | "Black Metal"
    "Black Metal \n "  | "Black Metal"
  }
}

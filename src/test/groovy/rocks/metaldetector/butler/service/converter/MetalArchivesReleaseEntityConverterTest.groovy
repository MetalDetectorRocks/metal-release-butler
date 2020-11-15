package rocks.metaldetector.butler.service.converter

import rocks.metaldetector.butler.model.release.ReleaseEntity
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH

class MetalArchivesReleaseEntityConverterTest extends Specification {

  MetalArchivesReleaseEntityConverter underTest = new MetalArchivesReleaseEntityConverter()

  def "Should convert raw data into ReleaseEntity"() {
    given:
    def artist = '<a href="http://www.example.com/band">The Band</a>'
    def albumTitle = '<a href="http://www.example.com/album">The Album Title</a>'
    def type = "Full-length"
    def genre = "Depressive Black Metal"
    def releaseDate = "August 26th, 2019"
    String[] rawReleaseData = [artist, albumTitle, type, genre, releaseDate]

    when:
    List<ReleaseEntity> conversionResult = underTest.convert(rawReleaseData)

    then:
    conversionResult.size() == 1

    and:
    conversionResult[0].artist == "The Band"
    conversionResult[0].metalArchivesArtistUrl == "http://www.example.com/band"
    conversionResult[0].additionalArtists.isEmpty()
    conversionResult[0].albumTitle =="The Album Title"
    conversionResult[0].coverSourceUrl == "http://www.example.com/album"
    conversionResult[0].type == FULL_LENGTH
    conversionResult[0].genre == "Depressive Black Metal"
    conversionResult[0].releaseDate == LocalDate.of(2019, 8, 26)
    conversionResult[0].source == METAL_ARCHIVES
    conversionResult[0].estimatedReleaseDate == null
  }

  def "Should handle null on index 2, 3 and 4" () {
    given:
    def artist = '<a href="http://www.example.com/band">The Band</a>'
    def albumTitle = '<a href="http://www.example.com/album">The Album Title</a>'
    String[] rawReleaseData = [artist, albumTitle, null, null, null]

    when:
    List<ReleaseEntity> conversionResult = underTest.convert(rawReleaseData)

    then:
    conversionResult.size() == 1

    and:
    conversionResult[0].type == null
    conversionResult[0].genre == null
    conversionResult[0].estimatedReleaseDate == null
  }

  def "Should convert raw data with an ampersand in band and album name"() {
    given:
    def artist = '<a href="https://www.example.com/band">The & Band</a>'
    def albumTitle = '<a href="https://www.example.com/album">White & Black</a>'
    def releaseDate = "October 1st, 2019"
    String[] rawReleaseData = [artist, albumTitle, null, null, releaseDate]

    when:
    List<ReleaseEntity> conversionResult = underTest.convert(rawReleaseData)

    then:
    conversionResult.size() == 1

    and:
    conversionResult[0].artist == "The & Band"
    conversionResult[0].albumTitle == "White & Black"
  }

  def "Converting raw data with three bands (Split Album) should work" () {
    given:
    def firstArtistName = "The 1st Artist"
    def secondArtistName = "The 2nd Artist"
    def thirdArtistName = "The 3rd Artist"
    def firstArtistUrl = "http://www.example.com/band1"
    def secondArtistUrl = "http://www.example.com/band2"
    def thirdArtistUrl = "http://www.example.com/band3"
    def artist = """
      <a href=\\\"$firstArtistUrl\\\">$firstArtistName</a> /
      <a href=\\"$secondArtistUrl\\">$secondArtistName</a> /
      <a href=\\"$thirdArtistUrl\\">$thirdArtistName</a>
    """
    def albumTitle = '<a href="http://www.example.com/album">The Album</a>'
    String[] rawReleaseData = [artist, albumTitle, null, null, null]

    when:
    List<ReleaseEntity> conversionResult = new MetalArchivesReleaseEntityConverter().convert(rawReleaseData)

    then:
    conversionResult.size() == 3

    and:
    conversionResult[0].artist == firstArtistName
    conversionResult[0].metalArchivesArtistUrl == firstArtistUrl
    conversionResult[0].additionalArtists == [secondArtistName, thirdArtistName]

    and:
    conversionResult[1].artist == secondArtistName
    conversionResult[1].metalArchivesArtistUrl == secondArtistUrl
    conversionResult[1].additionalArtists == [firstArtistName, thirdArtistName]

    and:
    conversionResult[2].artist == thirdArtistName
    conversionResult[2].metalArchivesArtistUrl == thirdArtistUrl
    conversionResult[2].additionalArtists == [firstArtistName, secondArtistName]
  }

  @Unroll
  "Should parse '#releaseDateAsString' to '#expectedReleaseDate' (focus: day of the month)" () {
    when:
    def releaseDate = underTest.parseReleaseDate(releaseDateAsString)

    then:
    releaseDate == expectedReleaseDate

    where:
    releaseDateAsString  | expectedReleaseDate
    "January 1st, 2020"  | LocalDate.of(2020, 1, 1)
    "January 2nd, 2020"  | LocalDate.of(2020, 1, 2)
    "January 3rd, 2020"  | LocalDate.of(2020, 1, 3)
    "January 4th, 2020"  | LocalDate.of(2020, 1, 4)
    "January 10th, 2020" | LocalDate.of(2020, 1, 10)
    "January 21st, 2020" | LocalDate.of(2020, 1, 21)
    "January 22nd, 2020" | LocalDate.of(2020, 1, 22)
    "January 23rd, 2020" | LocalDate.of(2020, 1, 23)
  }

  @Unroll
  "Should parse '#releaseDateAsString' to '#expectedReleaseDate' (focus: month of the year)" () {
    when:
    def releaseDate = underTest.parseReleaseDate(releaseDateAsString)

    then:
    releaseDate == expectedReleaseDate

    where:
    releaseDateAsString   | expectedReleaseDate
    "January 1st, 2020"   | LocalDate.of(2020, 1, 1)
    "February 1st, 2020"  | LocalDate.of(2020, 2, 1)
    "March 1st, 2020"     | LocalDate.of(2020, 3, 1)
    "April 1st, 2020"     | LocalDate.of(2020, 4, 1)
    "May 1st, 2020"       | LocalDate.of(2020, 5, 1)
    "June 1st, 2020"      | LocalDate.of(2020, 6, 1)
    "July 1st, 2020"      | LocalDate.of(2020, 7, 1)
    "August 1st, 2020"    | LocalDate.of(2020, 8, 1)
    "September 1st, 2020" | LocalDate.of(2020, 9, 1)
    "October 1st, 2020"   | LocalDate.of(2020, 10, 1)
    "November 1st, 2020"  | LocalDate.of(2020, 11, 1)
    "December 1st, 2020"  | LocalDate.of(2020, 12, 1)
  }

  @Unroll
  "Should parse '#releaseDateAsString' to '#expectedReleaseDate' (focus: year)" () {
    when:
    def releaseDate = underTest.parseReleaseDate(releaseDateAsString)

    then:
    releaseDate == expectedReleaseDate

    where:
    releaseDateAsString  | expectedReleaseDate
    "January 1st, 2020"  | LocalDate.of(2020, 1, 1)
    "January 1st, 2021"  | LocalDate.of(2021, 1, 1)
    "January 1st, 2022"  | LocalDate.of(2022, 1, 1)
  }

  def "Should return empty list when exception occurred" () {
    when:
    def response = underTest.convert(null)

    then:
    noExceptionThrown()

    and:
    response == []
  }
}

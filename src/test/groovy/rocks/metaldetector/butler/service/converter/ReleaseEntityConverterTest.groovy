package rocks.metaldetector.butler.service.converter

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseType

import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH

class ReleaseEntityConverterTest implements WithAssertions {

  @Test
  @DisplayName("Converting simple raw data should work")
  void convert_simple_raw_data_should_be_ok() throws Exception {
    // given
    def artist = "<a href=\\\"https://www.dummy.com/artists/band-name/123456789\\\">The Band</a>"
    def albumTitle = "<a href=\\\"https://www.dummy.com/albums/band-name/album-title/123456789\\\">The Album Title</a>"
    def type = "Full-length"
    def genre = "Depressive Black Metal"
    def releaseDate = "August 26th, 2019"
    String[] rawReleaseData = [artist, albumTitle, type, genre, releaseDate]

    // when
    List<ReleaseEntity> conversionResult = new ReleaseEntityConverter().convert(rawReleaseData)

    // then
    assertThat(conversionResult).hasSize(1)
    assertThat(conversionResult[0].artist).isEqualTo("The Band")
    assertThat(conversionResult[0].metalArchivesArtistUrl).isEqualTo(new URL("https://www.dummy.com/artists/band-name/123456789"))
    assertThat(conversionResult[0].additionalArtists).isEmpty()
    assertThat(conversionResult[0].albumTitle).isEqualTo("The Album Title")
    assertThat(conversionResult[0].metalArchivesAlbumUrl).isEqualTo(new URL("https://www.dummy.com/albums/band-name/album-title/123456789"))
    assertThat(conversionResult[0].type.name()).isEqualTo(FULL_LENGTH.name())
    assertThat(conversionResult[0].genre).isEqualTo("Depressive Black Metal")
    assertThat(conversionResult[0].releaseDate).isEqualTo(LocalDate.of(2019, 8, 26))
    assertThat(conversionResult[0].estimatedReleaseDate).isNull()
    assertThat(conversionResult[0].source.name()).isEqualTo(METAL_ARCHIVES.name())
  }

  @Test
  @DisplayName("Converting raw data with an ampersand in band name and/or album name should work")
  void convert_raw_data_with_ampersand_in_name() {
    // given
    def artist = "<a href=\\\"https://www.dummy.com/artists/band-name/123456789\\\">The & Band</a>"
    def albumTitle = "<a href=\\\"https://www.dummy.com/albums/band-name/album-title/123456789\\\">White & Black</a>"
    def type = "EP"
    def genre = "Heavy Metal"
    def releaseDate = "October 1st, 2019"
    String[] rawReleaseData = [artist, albumTitle, type, genre, releaseDate]

    // when
    List<ReleaseEntity> conversionResult = new ReleaseEntityConverter().convert(rawReleaseData)

    // then
    assertThat(conversionResult).hasSize(1)
    assertThat(conversionResult[0].artist).isEqualTo("The & Band")
    assertThat(conversionResult[0].metalArchivesArtistUrl).isEqualTo(new URL("https://www.dummy.com/artists/band-name/123456789"))
    assertThat(conversionResult[0].additionalArtists).isEmpty()
    assertThat(conversionResult[0].albumTitle).isEqualTo("White & Black")
    assertThat(conversionResult[0].metalArchivesAlbumUrl).isEqualTo(new URL("https://www.dummy.com/albums/band-name/album-title/123456789"))
    assertThat(conversionResult[0].type.name()).isEqualTo(ReleaseType.EP.name())
    assertThat(conversionResult[0].genre).isEqualTo("Heavy Metal")
    assertThat(conversionResult[0].releaseDate).isEqualTo(LocalDate.of(2019, 10, 1))
    assertThat(conversionResult[0].estimatedReleaseDate).isNull()
    assertThat(conversionResult[0].source.name()).isEqualTo(METAL_ARCHIVES.name())
  }

  @Test
  @DisplayName("Converting raw data with two bands (so called 'Split Album') should work")
  void convert_raw_data_with_two_bands() {
    // given
    def artist = """
      <a href=\\\"https://www.dummy.com/artists/band-name/123456789\\\">The 1st Band</a> / 
      <a href=\\"https://www.dummy.com/artists/band-name/1234567810\\">The 2nd Band</a>
    """
    def albumTitle = "<a href=\\\"https://www.dummy.com/albums/band-name/album-title/123456789\\\">The Album</a>"
    def type = "Full-length"
    def genre = "Heavy Metal"
    def releaseDate = "October 3rd, 2019"
    String[] rawReleaseData = [artist, albumTitle, type, genre, releaseDate]

    // when
    List<ReleaseEntity> conversionResult = new ReleaseEntityConverter().convert(rawReleaseData)

    // then
    assertThat(conversionResult).hasSize(2)

    assertThat(conversionResult[0].artist).isEqualTo("The 1st Band")
    assertThat(conversionResult[0].metalArchivesArtistUrl).isEqualTo(new URL("https://www.dummy.com/artists/band-name/123456789"))
    assertThat(conversionResult[0].additionalArtists).isEqualTo(["The 2nd Band"])

    assertThat(conversionResult[1].artist).isEqualTo("The 2nd Band")
    assertThat(conversionResult[1].metalArchivesArtistUrl).isEqualTo(new URL("https://www.dummy.com/artists/band-name/1234567810"))
    assertThat(conversionResult[1].additionalArtists).isEqualTo(["The 1st Band"])

    conversionResult.each {
      assertThat(it.albumTitle).isEqualTo("The Album")
      assertThat(it.metalArchivesAlbumUrl).isEqualTo(new URL("https://www.dummy.com/albums/band-name/album-title/123456789"))
      assertThat(it.type.name()).isEqualTo(FULL_LENGTH.name())
      assertThat(it.genre).isEqualTo("Heavy Metal")
      assertThat(it.releaseDate).isEqualTo(LocalDate.of(2019, 10, 3))
      assertThat(it.estimatedReleaseDate).isNull()
      assertThat(it.source.name()).isEqualTo(METAL_ARCHIVES.name())
    }
  }

  @Test
  @DisplayName("Converting raw data with three bands (so called 'Split Album') should work")
  void convert_raw_data_with_three_bands() {
    // given
    def artist = """
      <a href=\\\"https://www.dummy.com/artists/band-name/123456789\\\">The 1st Band</a> / 
      <a href=\\"https://www.dummy.com/artists/band-name/1234567810\\">The 2nd Band</a> / 
      <a href=\\"https://www.dummy.com/artists/band-name/1234567811\\">The 3rd Band</a>
    """
    def albumTitle = "<a href=\\\"https://www.dummy.com/albums/band-name/album-title/123456789\\\">The Album</a>"
    def type = "Full-length"
    def genre = "Heavy Metal"
    def releaseDate = "October 4th, 2019"
    String[] rawReleaseData = [artist, albumTitle, type, genre, releaseDate]

    // when
    List<ReleaseEntity> conversionResult = new ReleaseEntityConverter().convert(rawReleaseData)

    // then
    assertThat(conversionResult).hasSize(3)

    assertThat(conversionResult[0].artist).isEqualTo("The 1st Band")
    assertThat(conversionResult[0].metalArchivesArtistUrl).isEqualTo(new URL("https://www.dummy.com/artists/band-name/123456789"))
    assertThat(conversionResult[0].additionalArtists).isEqualTo(["The 2nd Band", "The 3rd Band"])

    assertThat(conversionResult[1].artist).isEqualTo("The 2nd Band")
    assertThat(conversionResult[1].metalArchivesArtistUrl).isEqualTo(new URL("https://www.dummy.com/artists/band-name/1234567810"))
    assertThat(conversionResult[1].additionalArtists).isEqualTo(["The 1st Band", "The 3rd Band"])

    assertThat(conversionResult[2].artist).isEqualTo("The 3rd Band")
    assertThat(conversionResult[2].metalArchivesArtistUrl).isEqualTo(new URL("https://www.dummy.com/artists/band-name/1234567811"))
    assertThat(conversionResult[2].additionalArtists).isEqualTo(["The 1st Band", "The 2nd Band"])

    conversionResult.each {
      assertThat(it.albumTitle).isEqualTo("The Album")
      assertThat(it.metalArchivesAlbumUrl).isEqualTo(new URL("https://www.dummy.com/albums/band-name/album-title/123456789"))
      assertThat(it.type.name()).isEqualTo(FULL_LENGTH.name())
      assertThat(it.genre).isEqualTo("Heavy Metal")
      assertThat(it.releaseDate).isEqualTo(LocalDate.of(2019, 10, 4))
      assertThat(it.estimatedReleaseDate).isNull()
      assertThat(it.source.name()).isEqualTo(METAL_ARCHIVES.name())
    }
  }

}

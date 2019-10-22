package com.metalr2.butler.service.parser

import com.metalr2.butler.web.dto.ReleaseDto
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import java.time.LocalDate

class ReleaseDtoConverterTest implements WithAssertions {

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
    List<ReleaseDto> conversionResult = new ReleaseDtoConverter().convert(rawReleaseData)

    // then
    assertThat(conversionResult).hasSize(1)
    assertThat(conversionResult[0].getArtist()).isEqualTo("The Band")
    assertThat(conversionResult[0].getArtistUrl()).isEqualTo(new URL("https://www.dummy.com/artists/band-name/123456789"))
    assertThat(conversionResult[0].getAdditionalArtists()).isEmpty()
    assertThat(conversionResult[0].getAlbumTitle()).isEqualTo("The Album Title")
    assertThat(conversionResult[0].getAlbumUrl()).isEqualTo(new URL("https://www.dummy.com/albums/band-name/album-title/123456789"))
    assertThat(conversionResult[0].getType()).isEqualTo("Full-length")
    assertThat(conversionResult[0].getGenre()).isEqualTo("Depressive Black Metal")
    assertThat(conversionResult[0].getReleaseDate()).isEqualTo(LocalDate.of(2019, 8, 26))
    assertThat(conversionResult[0].getEstimatedReleaseDate()).isNull()
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
    List<ReleaseDto> conversionResult = new ReleaseDtoConverter().convert(rawReleaseData)

    // then
    assertThat(conversionResult).hasSize(1)
    assertThat(conversionResult[0].getArtist()).isEqualTo("The & Band")
    assertThat(conversionResult[0].getArtistUrl()).isEqualTo(new URL("https://www.dummy.com/artists/band-name/123456789"))
    assertThat(conversionResult[0].getAdditionalArtists()).isEmpty()
    assertThat(conversionResult[0].getAlbumTitle()).isEqualTo("White & Black")
    assertThat(conversionResult[0].getAlbumUrl()).isEqualTo(new URL("https://www.dummy.com/albums/band-name/album-title/123456789"))
    assertThat(conversionResult[0].getType()).isEqualTo("EP")
    assertThat(conversionResult[0].getGenre()).isEqualTo("Heavy Metal")
    assertThat(conversionResult[0].getReleaseDate()).isEqualTo(LocalDate.of(2019, 10, 1))
    assertThat(conversionResult[0].getEstimatedReleaseDate()).isNull()
  }

  @Test
  @DisplayName("Converting raw data with two bands (so called 'Split Album') should work")
  void convert_raw_data_with_two_bands() {
    // <a href="https://www.metal-archives.com/bands/Grond/68353">Grond</a> / <a href="https://www.metal-archives.com/bands/Graceless/3540429440">Graceless</a>, <a href="https://www.metal-archives.com/albums/Grond_-_Graceless/Endless_Spiral_of_Terror/795963">Endless Spiral of Terror</a>, Split, Death Metal | Death/Doom Metal, October 30th, 2019]. Reason was: Content ist nicht zulässig in angehängtem Abschnitt.
    // given
    def artist = """
      <a href=\\\"https://www.dummy.com/artists/band-name/123456789\\\">The 1st Band</a> / 
      <a href=\\"https://www.dummy.com/artists/band-name/1234567810\\">The 2nd Band</a>
    """
    def albumTitle = "<a href=\\\"https://www.dummy.com/albums/band-name/album-title/123456789\\\">The Album</a>"
    def type = "Full-Length"
    def genre = "Heavy Metal"
    def releaseDate = "October 3rd, 2019"
    String[] rawReleaseData = [artist, albumTitle, type, genre, releaseDate]

    // when
    List<ReleaseDto> conversionResult = new ReleaseDtoConverter().convert(rawReleaseData)

    // then
    assertThat(conversionResult).hasSize(2)

    assertThat(conversionResult[0].getArtist()).isEqualTo("The 1st Band")
    assertThat(conversionResult[0].getArtistUrl()).isEqualTo(new URL("https://www.dummy.com/artists/band-name/123456789"))
    assertThat(conversionResult[0].getAdditionalArtists()).isEqualTo(["The 2nd Band"])

    assertThat(conversionResult[1].getArtist()).isEqualTo("The 2nd Band")
    assertThat(conversionResult[1].getArtistUrl()).isEqualTo(new URL("https://www.dummy.com/artists/band-name/1234567810"))
    assertThat(conversionResult[1].getAdditionalArtists()).isEqualTo(["The 1st Band"])

    for (int index in 0..(conversionResult.size() - 1)) {
      assertThat(conversionResult[index].getAlbumTitle()).isEqualTo("The Album")
      assertThat(conversionResult[index].getAlbumUrl()).isEqualTo(new URL("https://www.dummy.com/albums/band-name/album-title/123456789"))
      assertThat(conversionResult[index].getType()).isEqualTo("Full-Length")
      assertThat(conversionResult[index].getGenre()).isEqualTo("Heavy Metal")
      assertThat(conversionResult[index].getReleaseDate()).isEqualTo(LocalDate.of(2019, 10, 3))
      assertThat(conversionResult[index].getEstimatedReleaseDate()).isNull()
    }
  }

  @Test
  @DisplayName("Converting raw data with three bands (so called 'Split Album') should work")
  void convert_raw_data_with_three_bands() {
    // <a href="https://www.metal-archives.com/bands/Grond/68353">Grond</a> / <a href="https://www.metal-archives.com/bands/Graceless/3540429440">Graceless</a>, <a href="https://www.metal-archives.com/albums/Grond_-_Graceless/Endless_Spiral_of_Terror/795963">Endless Spiral of Terror</a>, Split, Death Metal | Death/Doom Metal, October 30th, 2019]. Reason was: Content ist nicht zulässig in angehängtem Abschnitt.
    // given
    def artist = """
      <a href=\\\"https://www.dummy.com/artists/band-name/123456789\\\">The 1st Band</a> / 
      <a href=\\"https://www.dummy.com/artists/band-name/1234567810\\">The 2nd Band</a> / 
      <a href=\\"https://www.dummy.com/artists/band-name/1234567811\\">The 3rd Band</a>
    """
    def albumTitle = "<a href=\\\"https://www.dummy.com/albums/band-name/album-title/123456789\\\">The Album</a>"
    def type = "Full-Length"
    def genre = "Heavy Metal"
    def releaseDate = "October 4th, 2019"
    String[] rawReleaseData = [artist, albumTitle, type, genre, releaseDate]

    // when
    List<ReleaseDto> conversionResult = new ReleaseDtoConverter().convert(rawReleaseData)

    // then
    assertThat(conversionResult).hasSize(3)

    assertThat(conversionResult[0].getArtist()).isEqualTo("The 1st Band")
    assertThat(conversionResult[0].getArtistUrl()).isEqualTo(new URL("https://www.dummy.com/artists/band-name/123456789"))
    assertThat(conversionResult[0].getAdditionalArtists()).isEqualTo(["The 2nd Band", "The 3rd Band"])

    assertThat(conversionResult[1].getArtist()).isEqualTo("The 2nd Band")
    assertThat(conversionResult[1].getArtistUrl()).isEqualTo(new URL("https://www.dummy.com/artists/band-name/1234567810"))
    assertThat(conversionResult[1].getAdditionalArtists()).isEqualTo(["The 1st Band", "The 3rd Band"])

    assertThat(conversionResult[2].getArtist()).isEqualTo("The 3rd Band")
    assertThat(conversionResult[2].getArtistUrl()).isEqualTo(new URL("https://www.dummy.com/artists/band-name/1234567811"))
    assertThat(conversionResult[2].getAdditionalArtists()).isEqualTo(["The 1st Band", "The 2nd Band"])

    for (int index in 0..(conversionResult.size() - 1)) {
      assertThat(conversionResult[0].getAlbumTitle()).isEqualTo("The Album")
      assertThat(conversionResult[0].getAlbumUrl()).isEqualTo(new URL("https://www.dummy.com/albums/band-name/album-title/123456789"))
      assertThat(conversionResult[0].getType()).isEqualTo("Full-Length")
      assertThat(conversionResult[0].getGenre()).isEqualTo("Heavy Metal")
      assertThat(conversionResult[0].getReleaseDate()).isEqualTo(LocalDate.of(2019, 10, 4))
      assertThat(conversionResult[0].getEstimatedReleaseDate()).isNull()
    }
  }

}

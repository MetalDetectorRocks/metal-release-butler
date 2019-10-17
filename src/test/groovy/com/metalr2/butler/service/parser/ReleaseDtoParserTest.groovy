package com.metalr2.butler.service.parser

import com.metalr2.butler.web.dto.ReleaseDto
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test

import java.time.LocalDate

class ReleaseDtoParserTest implements WithAssertions {

  @Test
  void test() throws Exception {
    // given
    def artist = "<a href=\\\"https://www.metal-archives.com/bands/Damnatus/3540414332\\\">Damnatus</a>"
    def albumTitle = "\"<a href=\\\"https://www.metal-archives.com/albums/Damnatus/Quando_nessuno_ti_aspetta_nel_mondo%E2%80%8B.%E2%80%8B.%E2%80%8B./799382\\\">Quando nessuno ti aspetta nel mondo...</a>"
    def type = "Full-length"
    def genre = "Depressive Black Metal"
    def releaseDate = "October 26th, 2019"
    String[] rawReleaseData = [artist, albumTitle, type, genre, releaseDate]

    // when
    ReleaseDto releaseDto = new ReleaseDtoParser(rawReleaseData).parse()

    // then
    assertThat(releaseDto.getArtist()).isEqualTo("Damnatus")
    assertThat(releaseDto.getArtistUrl()).isEqualTo(new URL("https://www.metal-archives.com/bands/Damnatus/3540414332"))
    assertThat(releaseDto.getAlbumTitle()).isEqualTo("Quando nessuno ti aspetta nel mondo...")
    assertThat(releaseDto.getAlbumUrl()).isEqualTo(new URL("https://www.metal-archives.com/albums/Damnatus/Quando_nessuno_ti_aspetta_nel_mondo%E2%80%8B.%E2%80%8B.%E2%80%8B./799382"))
    assertThat(releaseDto.getType()).isEqualTo("Full-length")
    assertThat(releaseDto.getGenre()).isEqualTo("Depressive Black Metal")
    assertThat(releaseDto.getReleaseDate()).isEqualTo(LocalDate.of(2019, 10, 26))
    assertThat(releaseDto.getEstimatedReleaseDate()).isNullOrEmpty()
  }

}

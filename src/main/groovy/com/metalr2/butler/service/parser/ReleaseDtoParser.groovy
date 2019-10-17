package com.metalr2.butler.service.parser

import com.metalr2.butler.web.dto.ReleaseDto

import java.time.LocalDate

class ReleaseDtoParser {

  final String[] rawData

  ReleaseDtoParser(String[] rawData) {
    this.rawData = rawData
  }

  ReleaseDto parse() {
    def artist = ""
    def artistUrl = new URL("http://www.dummy.com")
    def albumTitle = ""
    def albumUrl = new URL("http://www.dummy.com")
    def type = rawData[2]
    def genre = rawData[3]
    def releaseDate = LocalDate.now()

    return new ReleaseDto(artist: artist, artistUrl: artistUrl, albumTitle: albumTitle, albumUrl: albumUrl,
                          type: type, genre: genre, releaseDate: releaseDate)

  }

}

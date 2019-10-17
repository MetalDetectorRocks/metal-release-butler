package com.metalr2.butler.service.parser

import com.metalr2.butler.web.dto.ReleaseDto
import org.springframework.stereotype.Component

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class ReleaseDtoParser {

  final XmlSlurper xmlSlurper

  ReleaseDtoParser() {
    xmlSlurper = new XmlSlurper()
  }

  ReleaseDto parse(List<String> rawData) {
    def artist = parseAnchorName(rawData[0])
    def artistUrl = parseAnchorHref(rawData[0])
    def albumTitle = parseAnchorName(rawData[1])
    def albumUrl = parseAnchorHref(rawData[1])
    def type = rawData[2]
    def genre = rawData[3]
    def releaseDate = parseReleaseDate(rawData[4])

    return new ReleaseDto(artist: artist, artistUrl: artistUrl, albumTitle: albumTitle, albumUrl: albumUrl,
                          type: type, genre: genre, releaseDate: releaseDate)

  }

  /*
   * The double quotes of the href are masked with a backslash
   */
  private String removeEscapeCharacters(String text) {
    return text.replaceAll("\\\\\"", "\"")
  }

  private String parseAnchorName(String text) {
    text = removeEscapeCharacters(text)
    def xml = xmlSlurper.parseText(text)

    return xml.text()
  }

  private URL parseAnchorHref(String text) {
    text = removeEscapeCharacters(text)
    def xml = xmlSlurper.parseText(text)

    return new URL(xml.@href.text())
  }

  /*
   * In the raw data, the date is in the following format: [Month name] [dd][st|nd|rd|th], [yyyy]
   */
  private LocalDate parseReleaseDate(String rawDate) {
    rawDate = rawDate.replaceAll("(th)|(nd)|(rd)|(st)*", "") // ToDo DanielW: will not work for August
    def formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

    return LocalDate.parse(rawDate, formatter)
  }

}

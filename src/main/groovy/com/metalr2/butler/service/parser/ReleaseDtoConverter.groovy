package com.metalr2.butler.service.parser

import com.metalr2.butler.web.dto.ReleaseDto
import org.springframework.stereotype.Component

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class ReleaseDtoConverter {

  static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

  final XmlSlurper xmlSlurper

  ReleaseDtoConverter() {
    xmlSlurper = new XmlSlurper()
  }

  List<ReleaseDto> convert(String[] rawData) {
    def artistInfo = prepareXml(rawData[0])
    def albumInfo  = prepareXml(rawData[1])

    def artistName = parseAnchorName(artistInfo)
    def artistUrl = parseAnchorHref(artistInfo)
    def albumTitle = parseAnchorName(albumInfo)
    def albumUrl = parseAnchorHref(albumInfo)
    def type = rawData[2]
    def genre = rawData[3]
    def releaseDate = parseReleaseDate(rawData[4])

    return [new ReleaseDto(artist: artistName, artistUrl: artistUrl, albumTitle: albumTitle, albumUrl: albumUrl,
                          type: type, genre: genre, releaseDate: releaseDate)]
  }

  private String prepareXml(String text) {
    text = removeEscapeCharacters(text)
    text = encodeSpecialCharacters(text)
    return text
  }

  /*
   * The double quotes of the href are masked with a backslash
   */
  private String removeEscapeCharacters(String text) {
    return text.replaceAll("\\\\\"", "\"")
  }

  /*
   * A single & is illegal in an XML document when using XmlSlurper
   */
  private String encodeSpecialCharacters(String text) {
    return text.replaceAll("&", "&amp;")
  }

  private String parseAnchorName(String text) {
    def xml = xmlSlurper.parseText(text)

    return xml.text().trim()
  }

  private URL parseAnchorHref(String text) {
    def xml = xmlSlurper.parseText(text)

    return new URL(xml.@href.text())
  }

  private LocalDate parseReleaseDate(String rawDate) {
    rawDate = replaceDateSuffix(rawDate)
    return LocalDate.parse(rawDate, FORMATTER)
  }

  /*
   * In the raw data, the date is in the following format: [Month name] [dd][st|nd|rd|th], [yyyy]
   * Example: August 1st, 2019
   * The following suffixes must be removed: st, nd, rd, th
   */
  private String replaceDateSuffix(String rawDate) {
    def rawDateParts = rawDate.split(" ")
    rawDateParts[1] = rawDateParts[1].replaceAll("(th)|(nd)|(rd)|(st)*", "")
    return rawDateParts.join(" ")
  }

}

package rocks.metaldetector.butler.service.converter

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.model.release.ReleaseType

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static rocks.metaldetector.butler.model.release.ReleaseEntityState.OK
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES

@Component
@Slf4j
class MetalArchivesReleaseEntityConverter implements Converter<String[], List<ReleaseEntity>> {

  static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

  @Autowired
  XmlSlurper xmlSlurper

  /*
   * Returns a list of ReleaseEntity, since a split album from Metal Archives
   * is converted to a ReleaseEntity per each involved artist.
   */
  List<ReleaseEntity> convert(String[] rawData) {
    try {
      doConvert(rawData)
    }
    catch (Exception e) {
      log.error("Could not parse the following data: {}. Reason was: {}", rawData, e.getMessage())
      return []
    }
  }

  List<ReleaseEntity> doConvert(String[] rawData) {
    def releaseEntities = []
    def artistRawData   = prepareXml(rawData[0]) // Can contain multiple artists
    def albumRawData    = prepareXml(rawData[1])

    for (String artistInfo in splitArtistRawData(artistRawData)) {
      def artistName  = parseAnchorName(artistInfo)
      def artistUrl   = parseAnchorHref(artistInfo)
      def albumTitle  = parseAnchorName(albumRawData)
      def albumUrl    = parseAnchorHref(albumRawData)
      def type        = rawData[2]
      def genre       = rawData[3]
      def releaseDate = rawData[4] ? parseReleaseDate(rawData[4]) : null

      releaseEntities <<  new ReleaseEntity(
          artist: artistName,
          artistDetailsUrl: artistUrl,
          albumTitle: albumTitle,
          releaseDetailsUrl: albumUrl,
          type: ReleaseType.convertFrom(type),
          genre: genre,
          releaseDate: releaseDate,
          source: METAL_ARCHIVES,
          state: OK
      )
    }

    addAdditionalArtistInfo(releaseEntities)

    return releaseEntities
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

  /*
   * If an album comes from several artists, they are listed in the form
   * <a href="#">Band 1</a> / <a href="#">Band 2</a>.
   */
  private List<String> splitArtistRawData(String artistRawData) {
    artistRawData.split("</a> /").collect {
      it = it.trim()
      if (! it.endsWith("</a>")) {
        return it + "</a>"
      }
      return it
    }
  }

  private String parseAnchorName(String text) {
    def xml = xmlSlurper.parseText(text)

    return xml.text().trim()
  }

  private String parseAnchorHref(String text) {
    def xml = xmlSlurper.parseText(text)

    return xml.@href.text()
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
    rawDateParts[1] = rawDateParts[1].replaceAll("(th)|(nd)|(rd)|(st)", "")
    return rawDateParts.join(" ")
  }

  private void addAdditionalArtistInfo(List<ReleaseEntity> releaseEntities) {
    def artistNames = releaseEntities.collect { it.artist }
    releaseEntities.each {
      def additionalArtists = artistNames - it.artist
      if (! additionalArtists.isEmpty()) {
        it.additionalArtists = additionalArtists.join(", ")
      }
    }
  }
}

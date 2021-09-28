package rocks.metaldetector.butler.persistence.domain.release

enum ReleaseSource {

  METAL_ARCHIVES("Encyclopaedia Metallum: The Metal Archives", new URL("https://www.metal-archives.com/release/upcoming")),
  TIME_FOR_METAL("Time for Metal", new URL("https://time-for-metal.eu/metal-releases-kalender/")),
  TEST("Test", new URL("http://rest-release-source.com"))

  final String displayName
  final URL siteUrl

  ReleaseSource(String displayName, URL siteUrl) {
    this.displayName = displayName
    this.siteUrl = siteUrl
  }
}

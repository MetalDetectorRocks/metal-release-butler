package rocks.metaldetector.butler.model.release

enum ReleaseSource {

  METAL_ARCHIVES("Encyclopaedia Metallum: The Metal Archives", new URL("https://www.metal-archives.com/release/upcoming")),
  METAL_HAMMER_DE("Metal Hammer Germany", new URL("https://www.metal-hammer.de/neue-metal-alben-kommende-veroeffentlichungen-1032003/"))

  final String name
  final URL siteUrl

  ReleaseSource(String name, URL siteUrl) {
    this.name = name
    this.siteUrl = siteUrl
  }

}
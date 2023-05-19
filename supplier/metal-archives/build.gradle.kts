dependencies {
  implementation(libs.springBootStarterWeb)

  implementation(libs.groovy)
  implementation(libs.groovyXml)

  implementation(libs.jsoup)

  implementation(projects.persistence)
  implementation(projects.supplier.infrastructure)

  testImplementation(libs.bundles.testing)
}

description = "metal-archives"

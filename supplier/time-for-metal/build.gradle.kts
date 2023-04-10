dependencies {
  implementation(libs.springBootStarterWeb)
  implementation(libs.groovy)
  implementation(libs.groovyXml)
  implementation(libs.tagsoup)

  implementation(projects.persistence)
  implementation(projects.supplier.infrastructure)

  testImplementation(libs.bundles.testing)
}

description = "time-for-metal"

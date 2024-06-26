plugins {
  `java-library`
}

dependencies {
  implementation(libs.springBootStarterWeb)
  implementation(libs.commonsIo)
  implementation(libs.groovy)
  implementation(libs.groovyXml)
  implementation(libs.bundles.springCloudStarter)

  implementation(projects.persistence)

  api(libs.tagsoup)

  testImplementation(libs.bundles.testing)
}

description = "infrastructure"

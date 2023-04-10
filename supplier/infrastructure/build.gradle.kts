plugins {
  id("java-library")
}

dependencies {
  implementation(libs.springBootStarterWeb)
  implementation(libs.commonsIo)
  implementation(libs.groovy)
  implementation(libs.groovyXml)
  implementation(libs.s3)

  implementation(projects.persistence)

  api(libs.tagsoup)

  testImplementation(libs.bundles.testing)
}

description = "infrastructure"

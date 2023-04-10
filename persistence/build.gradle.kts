plugins {
  id("java-library")
}

dependencies {
  api(libs.springBootStarterDataJpa)

  implementation(libs.groovy)

  runtimeOnly(libs.postgres)

  testImplementation(libs.bundles.testing)
  testRuntimeOnly(libs.h2)
}

description = "persistence"

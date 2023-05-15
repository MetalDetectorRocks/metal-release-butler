import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
  alias(libs.plugins.springBootPlugin)
  alias(libs.plugins.dockerPublishPlugin)
}

dockerPublish {
  organisation.set("metaldetectorrocks")
  imageName.set(rootProject.name)
}

springBoot {
  mainClass.set("rocks.metaldetector.butler.MetalReleaseButlerApplication")
  buildInfo().apply {
    version = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"))
  }
}

tasks {
  bootJar {
    archiveClassifier.set("boot")
    enabled = true
  }

  jar {
    enabled = false
  }
}

dependencies {
  implementation(libs.bundles.springBootStarterApp)

  implementation(libs.httpClient)
  implementation(libs.micrometerRegistryPrometheus)
  implementation(libs.flyway)
  implementation(libs.bundles.jjwt)

  implementation(libs.bundles.groovyApp)

  implementation(projects.persistence)
  implementation(projects.supplier.infrastructure)
  implementation(projects.supplier.timeForMetal)
  implementation(projects.supplier.metalArchives)

  runtimeOnly(libs.lokiLogbackAppender)
  developmentOnly(libs.springBootDevTools)
  annotationProcessor(libs.springBootConfigurationProcessor)

  testImplementation(libs.springSecurityTest)
  testImplementation(libs.bundles.testing)
  testRuntimeOnly(libs.h2)
}

description = "app"

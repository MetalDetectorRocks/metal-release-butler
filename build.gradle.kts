import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

val javaVersion: JavaVersion = JavaVersion.VERSION_17

val dependencyVersions = listOf(
    "commons-logging:commons-logging:1.2",
    libs.junitBom.get().toString()
)

val dependencyGroupVersions = mapOf(
    libs.groovy.get().group to libs.groovy.get().version,
    libs.jupiterApi.get().group to libs.jupiterApi.get().version
)

plugins {
  id("org.springframework.boot") version "3.0.6" apply false
  id("io.spring.dependency-management") version "1.1.0" apply false
  id("de.europace.docker-publish") version "1.4.3" apply false
}

subprojects {
  project.apply(plugin = "groovy")
  project.apply(plugin = "io.spring.dependency-management")

  the<DependencyManagementExtension>().apply {
    imports {
      mavenBom(BOM_COORDINATES)
    }
  }

  configurations {
    all {
      resolutionStrategy {
        failOnVersionConflict()
        force(dependencyVersions)
        eachDependency {
          val forcedVersion = dependencyGroupVersions[requested.group]
          if (forcedVersion != null) {
            useVersion(forcedVersion)
          }
        }
        cacheDynamicVersionsFor(0, "seconds")
      }
    }
  }

  repositories {
    mavenCentral()
  }

  configure<JavaPluginExtension> {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
  }

  tasks {
    withType<Test> {
      useJUnitPlatform()
      testLogging.showStandardStreams = true
    }
    withType<GroovyCompile> {
      options.encoding = "UTF-8"
    }
  }
}

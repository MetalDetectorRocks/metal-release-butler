import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

val javaVersion: JavaVersion = JavaVersion.VERSION_17

val dependencyVersions = listOf(
    "commons-logging:commons-logging:1.2",
    "org.junit:junit-bom:${libs.versions.junit.get()}"
)

val dependencyGroupVersions = mapOf(
    "org.apache.groovy" to libs.versions.groovy.get(),
    "org.junit.jupiter" to libs.versions.junit.get()
)

plugins {
  id("org.springframework.boot") version "3.0.5" apply false
  id("io.spring.dependency-management") version "1.1.0" apply false
  id("de.europace.docker-publish") version "1.4.2" apply false
}

subprojects {
  project.apply(plugin = "groovy")
  project.apply(plugin = "io.spring.dependency-management")
  project.apply(plugin = "jacoco")

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
    withType<JacocoReport> {
      reports {
        xml.required.set(true)
        html.required.set(false)
      }
    }
    withType<GroovyCompile> {
      options.encoding = "UTF-8"
    }
  }
}

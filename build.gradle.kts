import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

val javaVersion: JavaVersion = JavaVersion.VERSION_21

val dependencyVersions = listOf(
  libs.junitBom
)

val dependencyGroupVersions = mapOf(
  libs.logbackClassic.get().group to libs.logbackClassic.get().version,
  libs.nettyCommon.get().group to libs.nettyCommon.get().version
)

plugins {
  alias(libs.plugins.springBoot) apply false
  alias(libs.plugins.springDependencyManagement) apply false
  alias(libs.plugins.dockerPublish) apply false
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

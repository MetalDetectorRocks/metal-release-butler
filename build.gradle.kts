val javaVersion: JavaVersion = JavaVersion.VERSION_17

val dependencyVersions = listOf(
    "commons-logging:commons-logging:1.2",
    "commons-codec:commons-codec:1.15",
    "org.ow2.asm:asm:9.2",
    "net.bytebuddy:byte-buddy:1.12.2",
    "org.assertj:assertj-core:3.21.0",
    "org.slf4j:slf4j-api:1.7.32",
    "org.apiguardian:apiguardian-api:1.1.2",
    "org.jboss.logging:jboss-logging:3.4.2.Final"
)

val dependencyGroupVersions = mapOf(
    "org.codehaus.groovy" to libs.versions.groovy.get(),
    "org.springframework" to libs.versions.spring.get(),
    "org.springframework.boot" to libs.versions.springBoot.get(),
    "com.fasterxml.jackson.core" to libs.versions.jackson.get(),
    "com.fasterxml.jackson.dataformat" to libs.versions.jackson.get(),
    "com.fasterxml.jackson.datatype" to libs.versions.jackson.get(),
    "com.fasterxml.jackson.module" to libs.versions.jackson.get(),
    "org.junit.jupiter" to libs.versions.junit.get(),
    "org.junit" to libs.versions.junit.get()
)

plugins {
  id("org.springframework.boot") version "2.6.1" apply false
  id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
}

subprojects {
  project.apply(plugin = "groovy")
  project.apply(plugin = "io.spring.dependency-management")
  project.apply(plugin = "jacoco")

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

tasks {
  wrapper {
    gradleVersion = "7.3"
    distributionType = Wrapper.DistributionType.ALL
  }
}

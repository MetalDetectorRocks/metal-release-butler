val javaVersion: JavaVersion = JavaVersion.VERSION_11
val dependencyVersions = listOf(
    "commons-lang:commons-lang:2.6",
    "commons-logging:commons-logging:1.2",
    "org.junit:junit-bom:5.7.2",
    "org.ow2.asm:asm:9.2",
    "org.objenesis:objenesis:3.2",
    "org.jetbrains:annotations:22.0.0"
)
val dependencyGroupVersions = mapOf(
    "org.codehaus.groovy" to libs.versions.groovy.get()
)

plugins {
  id("org.springframework.boot") version "2.5.4"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("groovy")
  id("jacoco")
}

springBoot {
  mainClass.set("rocks.metaldetector.butler.MetalReleaseButlerApplication")
}

group = "rocks.metaldetector"

repositories {
  mavenCentral()
}

configure<JavaPluginConvention> {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
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

tasks {
  withType<Test> {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
  }
  withType<JacocoReport> {
    reports {
      xml.isEnabled = true
      html.isEnabled = false
    }
  }
  jar {
    enabled = false
  }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  implementation("org.apache.httpcomponents:httpclient")
  implementation("commons-io:commons-io:${libs.versions.commonsIo.get()}")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.flywaydb:flyway-core")

  implementation("org.codehaus.groovy:groovy:${libs.versions.groovy.get()}")
  implementation("org.codehaus.groovy:groovy-xml:${libs.versions.groovy.get()}")
  implementation("org.codehaus.groovy:groovy-datetime:${libs.versions.groovy.get()}")
  implementation("org.codehaus.groovy.modules.http-builder:http-builder:${libs.versions.httpBuilder.get()}")

  implementation("io.springfox:springfox-swagger2:${libs.versions.swagger.get()}")
  implementation("io.springfox:springfox-swagger-ui:${libs.versions.swagger.get()}")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql:${libs.versions.postgresql.get()}")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  implementation("io.jsonwebtoken:jjwt:${libs.versions.jsonwebtoken.get()}")
  implementation("com.amazonaws:aws-java-sdk-s3:${libs.versions.awsS3Sdk.get()}")
  implementation("org.jsoup:jsoup:${libs.versions.jsoup.get()}")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "junit", module = "junit")
  }
  testImplementation("org.codehaus.groovy:groovy-test:${libs.versions.groovy.get()}")
  testImplementation("org.spockframework:spock-core:${libs.versions.spock.get()}")
  testImplementation("org.spockframework:spock-spring:${libs.versions.spock.get()}")
  testImplementation("org.springframework.security:spring-security-test")
}

tasks {
  wrapper {
    gradleVersion = "7.1.1"
    distributionType = Wrapper.DistributionType.ALL
  }
}

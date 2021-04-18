extra.apply {
  set("awsS3SdkVersion", "1.11.1000")
  set("commonsIoVersion", "2.8.0")
  set("groovyVersion", "3.0.7")
  set("httpBuilderVersion", "0.7.1")
  set("jsonwebtokenVersion", "0.9.1")
  set("jsoupVersion", "1.13.1")
  set("postgresqlVersion", "42.2.19")
  set("spockVersion", "2.0-M5-groovy-3.0")
  set("swaggerVersion", "3.0.0")
}

val javaVersion: JavaVersion = JavaVersion.VERSION_11
val dependencyVersions = listOf(
  "commons-lang:commons-lang:2.4",
  "commons-logging:commons-logging:1.2",
  "org.junit:junit-bom:5.7.1",
  "org.ow2.asm:asm:9.1",
  "org.objenesis:objenesis:3.2"
)
val dependencyGroupVersions = mapOf(
  "org.codehaus.groovy" to extra["groovyVersion"] as String
)

plugins {
  id("org.springframework.boot") version "2.4.5"
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
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  implementation("org.apache.httpcomponents:httpclient")
  implementation("commons-io:commons-io:${rootProject.extra["commonsIoVersion"]}")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.flywaydb:flyway-core")

  implementation("org.codehaus.groovy:groovy:${rootProject.extra["groovyVersion"]}")
  implementation("org.codehaus.groovy:groovy-xml:${rootProject.extra["groovyVersion"]}")
  implementation("org.codehaus.groovy:groovy-datetime:${rootProject.extra["groovyVersion"]}")
  implementation("org.codehaus.groovy.modules.http-builder:http-builder:${rootProject.extra["httpBuilderVersion"]}")

  implementation("io.springfox:springfox-swagger2:${rootProject.extra["swaggerVersion"]}")
  implementation("io.springfox:springfox-swagger-ui:${rootProject.extra["swaggerVersion"]}")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql:${rootProject.extra["postgresqlVersion"]}")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  implementation("io.jsonwebtoken:jjwt:${rootProject.extra["jsonwebtokenVersion"]}")
  implementation("com.amazonaws:aws-java-sdk-s3:${rootProject.extra["awsS3SdkVersion"]}")
  implementation("org.jsoup:jsoup:${rootProject.extra["jsoupVersion"]}")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "junit", module = "junit")
  }
  testImplementation("org.codehaus.groovy:groovy-test:${rootProject.extra["groovyVersion"]}")
  testImplementation("org.spockframework:spock-core:${rootProject.extra["spockVersion"]}")
  testImplementation("org.spockframework:spock-spring:${rootProject.extra["spockVersion"]}")
  testImplementation("org.springframework.security:spring-security-test")
}

tasks {
  wrapper {
    gradleVersion = "7.0"
    distributionType = Wrapper.DistributionType.ALL
  }
}

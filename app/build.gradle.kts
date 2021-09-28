plugins {
  id("org.springframework.boot")
}

springBoot {
  mainClass.set("rocks.metaldetector.butler.MetalReleaseButlerApplication")
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
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  implementation("org.apache.httpcomponents:httpclient")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.flywaydb:flyway-core")
  implementation("io.jsonwebtoken:jjwt:${libs.versions.jsonwebtoken.get()}")

  implementation("org.codehaus.groovy:groovy:${libs.versions.groovy.get()}")
  implementation("org.codehaus.groovy:groovy-xml:${libs.versions.groovy.get()}")
  implementation("org.codehaus.groovy:groovy-datetime:${libs.versions.groovy.get()}")

  implementation("io.springfox:springfox-swagger2:${libs.versions.swagger.get()}")
  implementation("io.springfox:springfox-swagger-ui:${libs.versions.swagger.get()}")

  implementation(projects.persistence)
  implementation(projects.supplier.infrastructure)
  implementation(projects.supplier.timeForMetal)
  implementation(projects.supplier.metalArchives)

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "junit", module = "junit")
  }
  testImplementation("org.codehaus.groovy:groovy-test:${libs.versions.groovy.get()}")
  testImplementation("org.spockframework:spock-core:${libs.versions.spock.get()}")
  testImplementation("org.spockframework:spock-spring:${libs.versions.spock.get()}")
  testImplementation("org.springframework.security:spring-security-test")
  testRuntimeOnly("com.h2database:h2:${libs.versions.h2.get()}")
}

description = "app"

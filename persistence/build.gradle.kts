plugins {
  id("java-library")
}

dependencies {
  api("org.springframework.boot:spring-boot-starter-data-jpa")

  implementation("org.apache.groovy:groovy:${libs.versions.groovy.get()}")

  runtimeOnly("org.postgresql:postgresql:${libs.versions.postgresql.get()}")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.apache.groovy:groovy-test:${libs.versions.groovy.get()}")
  testImplementation("org.spockframework:spock-core:${libs.versions.spock.get()}")
  testImplementation("org.spockframework:spock-spring:${libs.versions.spock.get()}")
  testRuntimeOnly("com.h2database:h2")
}

description = "persistence"

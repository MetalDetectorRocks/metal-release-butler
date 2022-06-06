plugins {
  id("java-library")
}

dependencies {
  api("org.springframework.boot:spring-boot-starter-data-jpa:${libs.versions.springBoot.get()}")

  implementation("org.apache.groovy:groovy:${libs.versions.groovy.get()}")

  runtimeOnly("org.postgresql:postgresql:${libs.versions.postgresql.get()}")

  testImplementation("org.springframework.boot:spring-boot-starter-test:${libs.versions.springBoot.get()}")
  testImplementation("org.apache.groovy:groovy-test:${libs.versions.groovy.get()}")
  testImplementation("org.spockframework:spock-core:${libs.versions.spock.get()}")
  testImplementation("org.spockframework:spock-spring:${libs.versions.spock.get()}")
  testRuntimeOnly("com.h2database:h2:${libs.versions.h2.get()}")
}

description = "persistence"

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-web")

  implementation("org.apache.groovy:groovy:${libs.versions.groovy.get()}")
  implementation("org.apache.groovy:groovy-xml:${libs.versions.groovy.get()}")

  implementation(projects.persistence)
  implementation(projects.supplier.infrastructure)

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.apache.groovy:groovy-test:${libs.versions.groovy.get()}")
  testImplementation("org.spockframework:spock-core:${libs.versions.spock.get()}")
  testImplementation("org.spockframework:spock-spring:${libs.versions.spock.get()}")
}

description = "time-for-metal"

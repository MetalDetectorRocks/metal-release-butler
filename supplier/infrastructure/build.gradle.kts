dependencies {

  implementation("org.springframework.boot:spring-boot-starter-web")

  implementation("commons-io:commons-io:${libs.versions.commonsIo.get()}")

  implementation("org.apache.groovy:groovy:${libs.versions.groovy.get()}")
  implementation("org.apache.groovy:groovy-xml:${libs.versions.groovy.get()}")

  implementation("com.amazonaws:aws-java-sdk-s3:${libs.versions.awsS3Sdk.get()}")

  implementation(projects.persistence)

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.apache.groovy:groovy-test:${libs.versions.groovy.get()}")
  testImplementation("org.spockframework:spock-core:${libs.versions.spock.get()}")
  testImplementation("org.spockframework:spock-spring:${libs.versions.spock.get()}")
}

description = "infrastructure"

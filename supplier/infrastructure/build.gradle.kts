dependencies {

  implementation("org.springframework.boot:spring-boot-starter-web")

  implementation("commons-io:commons-io:${libs.versions.commonsIo.get()}")

  implementation("org.codehaus.groovy:groovy:${libs.versions.groovy.get()}")
  implementation("org.codehaus.groovy:groovy-xml:${libs.versions.groovy.get()}")
  implementation("io.github.http-builder-ng:http-builder-ng-core:${libs.versions.httpBuilder.get()}")

  implementation("com.amazonaws:aws-java-sdk-s3:${libs.versions.awsS3Sdk.get()}")

  implementation(projects.persistence)

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "junit", module = "junit")
  }
  testImplementation("org.codehaus.groovy:groovy-test:${libs.versions.groovy.get()}")
  testImplementation("org.spockframework:spock-core:${libs.versions.spock.get()}")
  testImplementation("org.spockframework:spock-spring:${libs.versions.spock.get()}")
}

description = "infrastructure"

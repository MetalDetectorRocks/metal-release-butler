FROM openjdk:11-stretch

ARG BUILD_DATE
LABEL org.label-schema.build-date=$BUILD_DATE

COPY build/libs/metal-release-butler-0.0.1-SNAPSHOT.jar metal-release-butler.jar

RUN sh -c 'touch /metal-release-butler.jar'

EXPOSE 8095

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/metal-release-butler.jar"]

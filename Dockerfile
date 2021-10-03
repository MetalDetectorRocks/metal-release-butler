FROM openjdk:11-stretch

ENV TZ=Europe/Berlin
ENV SERVER_PORT 8080

RUN apt-get update && apt-get install -y \
  curl \
  && rm -rf /var/lib/apt/lists/*

RUN mkdir /app && mkdir /app/images && mkdir /app/logs
WORKDIR /app

RUN useradd --no-log-init --no-create-home --shell /bin/false service_user \
  && chown -cR service_user:service_user /app
USER service_user

EXPOSE $SERVER_PORT
VOLUME ["/app/images/", "/app/logs/"]

# Arguments
ARG SOURCE_JAR_FILE="app/build/libs/*.jar"
ARG BUILD_DATE
ARG VCS_REF

# Labels
LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.build-date=$BUILD_DATE
LABEL org.label-schema.name="metaldetector/metal-release-butler"
LABEL org.label-schema.description="Metal Release Butler application collects information about announced album releases of metal bands from external sources and made it available through a REST endpoint."
LABEL org.label-schema.maintainer="https://github.com/MetalDetectorRocks"
LABEL org.label-schema.url="https://metal-detector.rocks"
LABEL org.label-schema.vcs-url="https://github.com/MetalDetectorRocks/metal-release-butler"
LABEL org.label-schema.vcs-ref=$VCS_REF
LABEL org.label-schema.version=$BUILD_DATE

#HEALTHCHECK --start-period=30s --interval=10s --timeout=5s --retries=3 CMD curl --fail http://localhost:8080/actuator/health || exit 1

COPY $SOURCE_JAR_FILE app.jar
COPY docker-entrypoint.sh /app

ENTRYPOINT ["bash", "/app/docker-entrypoint.sh"]
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

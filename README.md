![Continous Integration](https://github.com/MetalDetectorRocks/metal-release-butler/workflows/Continous%20Integration/badge.svg)
![Docker Image](https://github.com/MetalDetectorRocks/metal-release-butler/workflows/Docker%20Image/badge.svg)

## Table of contents
1. [ Introduction ](#introduction)

2. [ Download source code ](#download-source-code)

3. [ Run application locally (DEV profile) ](#run-application-locally-dev)

4. [ Start the application ](#start-application)

5. [ API documentation ](#api-documentation)

<a name="introduction"></a>
## 1 Introduction
This repository contains the source code for the _Metal Release Butler_ microservice. The service is a Groovy based Spring Boot application. 

It collects information about announced album releases of metal bands from external sources. This information is persisted in a database and made available through a REST endpoint.  

<a name="download-source-code"></a>
## 2 Download source code

Clone the source code via:

```
git clone https://github.com/MetalDetectorRocks/metal-release-butler.git
```

<a name="run-application-locally-dev"></a>
## 3 Run application locally (DEV profile)

To start the application locally in DEV profile, the following preparatory actions are necessary:

1. Install Java 8

2. Install Groovy (v2.5.8 or higher)

3. Install Docker CE

4. Create folder `.secrets` below the project root directory and create the file `butler_db_root_password.txt` within this folder

5. Enter a password of your choice in the created file. The file is used for the `docker-compose.yml` file, which becomes relevant in a moment.

6. Run `docker-compose.yml` via command `docker-compose up -d --no-recreate`. This starts all peripheral docker containers that are needed locally to run the Metal Release Butler Application:
    - `butler-db`: The database for Metal Release Butler application 
    - `butler-phpmyadmin`: phpmyadmin for Metal Release Butlers database

7. Define the data source connection details in file `application.properties`:
    - `spring.datasource.username` (you have to use user `root`)
    - `spring.datasource.password` (password from `butler_db_root_password.txt`)
    - `spring.datasource.url` (`jdbc:mysql://localhost:3307/metal-release-butler?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false`, the database name must match `MYSQL_DATABASE` of service `butler-db` from `docker-compose.yml` file)

8. Configure the profile `dev` for example via your IntelliJ Run Configuration or via `spring.profiles.active=dev` in the file `application.properties`  

It is also possible to define all mentioned connection details and secrets as environment variables. In this case no .properties variables need to be changed. The names of the environment variables are already in the .properties files. You can define the environment variables for example within a Run Configuration in IntelliJ (other IDEs have similar possibilities).

<a name="start-application"></a>
## 4 Start the application

via Gradle
- Execute command `./gradlew bootRun` in root directory

via your IDE
- Execute main class `rocks.metaldetector.butler.MetalReleaseButlerApplication`

<a name="api-documentation"></a>
## 5 API documentation

A Swagger API documentation is coming soon...
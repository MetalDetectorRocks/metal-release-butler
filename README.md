![Continous Integration](https://github.com/MetalDetectorRocks/metal-release-butler/workflows/Continous%20Integration/badge.svg)
[![codecov](https://codecov.io/gh/MetalDetectorRocks/metal-release-butler/branch/master/graph/badge.svg)](https://codecov.io/gh/MetalDetectorRocks/metal-release-butler)
![Docker Image](https://github.com/MetalDetectorRocks/metal-release-butler/workflows/Docker%20Image/badge.svg)

## Table of contents
1. [ Introduction ](#introduction)

2. [ Download source code ](#download-source-code)

3. [ Run application locally ](#run-application-locally)

4. [ Start the application ](#start-application)

5. [ Execute tests locally ](#execute-tests-locally)

6. [ API documentation ](#api-documentation)

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

<a name="run-application-locally"></a>
## 3 Run application locally

To start the application locally, the following preparatory actions are necessary:

1. Install Java 11

2. Install Groovy (v3.0.2 or higher)

3. Install Docker CE

4. Create folder `.secrets` below the project root directory and create the file `butler_db_root_password.txt` within this folder

5. Enter a password of your choice in the created file. The file is used for the `docker-compose.yml` file, which becomes relevant in a moment.

6. Run `docker-compose.yml` via command `docker-compose up -d --no-recreate`. This starts all peripheral docker containers that are needed locally to run the Metal Release Butler Application:
    - `butler-db`: The database for Metal Release Butler application

7. Define the data source connection details in file `application.yml`:
    - `spring.datasource.username` (you have to use user `postgres`)
    - `spring.datasource.password` (password from `butler_db_root_password.txt`)
    - `spring.datasource.url` (`jdbc:postgresql://localhost:5432/metal-release-butler`, the database name must match `POSTGRES_DB` of service `butler-db` from `docker-compose.yml` file)

It is also possible to define all mentioned connection details and secrets as environment variables. In this case no variables in `application.yml` need to be changed. The names of the environment variables are already in the `application.yml` file. You can define the environment variables for example within a Run Configuration in IntelliJ (other IDEs have similar possibilities).

<a name="start-application"></a>
## 4 Start the application

via gradle
- Execute command `./gradlew bootRun` in root directory

via your IDE
- Execute main class `rocks.metaldetector.butler.MetalReleaseButlerApplication`

<a name="execute-tests-locally"></a>
## 5 Execute tests locally

via gradle
- Execute command `./gradlew clean test` in root directory

via your IDE
- Execute the task `test` from folder `verification`
- Please note: You might get the message "Test events were not received" if you do this via IntelliJ. This is intentional behaviour of gradle. If nothing changes in the tests themselves, they will not be executed repeatedly. If you still want to run the tests, you have to execute `clean` before.

<a name="api-documentation"></a>
## 6 API documentation

A Swagger API documentation is coming soon...
![Continous Integration](https://github.com/MetalDetectorRocks/metal-release-butler/workflows/Continous%20Integration/badge.svg)
[![codecov](https://codecov.io/gh/MetalDetectorRocks/metal-release-butler/branch/master/graph/badge.svg)](https://codecov.io/gh/MetalDetectorRocks/metal-release-butler)
![Docker Image](https://github.com/MetalDetectorRocks/metal-release-butler/workflows/Docker%20Image/badge.svg)

## Table of contents
1. [ Introduction ](#introduction)

2. [ Download source code ](#download-source-code)

3. [ Run application locally ](#run-application-locally)

4. [ Generate access token ](#generate-access-token)

5. [ Start the application ](#start-application)

6. [ Execute tests locally ](#execute-tests-locally)

7. [ API documentation ](#api-documentation)

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

4. Run `docker-compose up -d --no-recreate` from the root directory of the project. This starts a postgresql database that is needed locally to run the Metal Release Butler Application.

5. Define the data source connection details in file `application.yml`:
    - `spring.datasource.username` (you have to use user `postgres`)
    - `spring.datasource.password` (password from `docker-compose.yml`)
    - `spring.datasource.url` (`jdbc:postgresql://localhost:5432/metal-release-butler`, the database name must match `POSTGRES_DB` of service `butler-db` from `docker-compose.yml` file)
    - `security.token-secret` (choose any value you want)

It is also possible to define all mentioned connection details and secrets as environment variables. In this case no variables in `application.yml` need to be changed. The names of the environment variables are already in the `application.yml` file. You can define the environment variables for example within a Run Configuration in IntelliJ (other IDEs have similar possibilities).

<a name="generate-access-token"></a>
## 4 Generate Access Token

The endpoints are secured with a static Json Web Token. Use the groovy script 'create-detector-jwt.groovy' to create a token for a local setup. You can find the script in folder `src/main/resources/config`.
Before executing the script, the environment variable `JWT_SECRET` must be exposed. Please use the value from `security.token-secret` in `application.yml`.
The generated token is displayed on the console after script execution. Send the token with every request in the `Authorization` Header as Bearer Token.

To disable authorization, start the application with the environment variable 'ROCKS_METALDETECTOR_AUTHENTICATION_ENABLED' and the value 'FALSE'.

<a name="start-application"></a>
## 5 Start the application

via gradle
- Execute command `./gradlew bootRun` in root directory

via your IDE
- Execute main class `rocks.metaldetector.butler.MetalReleaseButlerApplication`

<a name="execute-tests-locally"></a>
## 6 Execute tests locally

via gradle
- Execute command `./gradlew clean test` in root directory

via your IDE
- Execute the task `test` from folder `verification`
- Please note: You might get the message "Test events were not received" if you do this via IntelliJ. This is intentional behaviour of gradle. If nothing changes in the tests themselves, they will not be executed repeatedly. If you still want to run the tests, you have to execute `clean` before.

<a name="api-documentation"></a>
## 7 API documentation

A Swagger API documentation is coming soon...
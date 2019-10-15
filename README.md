# IoT Server Reporting Service

## Service
Part of the [tlvlp IoT project](https://github.com/tlvlp/iot-project-summary)'s server side microservices.

This Dockerized SpringBoot-based service is responsible for all reporting related tasks:
- Exposes API for saving new value entries
- Exposes API for querying reports
- Generates rolling averages for HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY scopes

## Building and publishing JAR + Docker image
This project is using the [Palantir Docker Gradle plugin](https://github.com/palantir/gradle-docker).
All configuration can be found in the [Gradle build file](build.gradle) file 
and is recommended to be run with the docker/dockerTagsPush task.

## Dockerhub
Repository: [tlvlp/iot-reporting-service](https://cloud.docker.com/repository/docker/tlvlp/iot-reporting-service)


## Deployment
- This service is currently designed as **stateless** and can have an arbitrary number of instances running per Docker Swarm Stack.
- For settings and deployment details see the project's [deployment repository](https://github.com/tlvlp/iot-server-deployment)

## Server-side API
Actual API endpoints are inherited from the project's [deployment repository](https://github.com/tlvlp/iot-server-deployment) via environment variables.

> API documentation has been temporarily removed!


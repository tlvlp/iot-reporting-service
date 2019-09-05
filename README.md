# IoT Server Reporting Service

## Service
Part of the tlvlp IoT project's server side microservices.

This Dockerized SpringBoot-based service is responsible for all reporting related tasks:
- Exposes API for saving new value entries
- Exposes API for querying reports
- Generates rolling averages for HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY scopes

## Building and publishing JAR + Docker image
This project is using the using the [Palantir Docker Gradle plugin](https://github.com/palantir/gradle-docker).
All configuration can be found in the [Gradle build file](build.gradle) file 
and is recommended to be run with the docker/dockerTagsPush task.

## Deployment
- This service is currently designed as **stateless** and can have an arbitrary number of instances running per Docker Swarm Stack.
- For settings and deployment details see the project's [deployment repository](https://gitlab.com/tlvlp/iot.server.deployment)

## Server-side API
Actual API endpoints are inherited from the project's [deployment repository](https://gitlab.com/tlvlp/iot.server.deployment) via environment variables.

### POST Values:

Returns a multi-status response with a Map where the keys are the posted values and the values are the result statuses

#### Related environment variables:
- ${REPORTING_SERVICE_API_POST_VALUES}
- ${REPORTING_SERVICE_API_POST_VALUES_URL}

#### Fields:
Takes a List in the RequestBody where each value must contain:
- **unitID**: String - ID of the containing Unit
- **moduleID**: String - module ID
- **value**: Double - value/state of the Module

```
{ 
    [   
        {
            "unitID": "tlvlp.iot.BazsalikON-soil",
            "moduleID": "relay|growlight",
            "value": 1
        },
        {
            "unitID": "tlvlp.iot.BazsalikON-soil",
            "moduleID": "gl5528|lightPercent", 
            "value": 85
        {
    ]
}

```

### GET Average Value Reports:

Returns a Map where the keys are the ChronoUnits of the requested scopes and the values are the
maps containing Dates in string format as keys and the corresponding calculated averages as Double values

#### Related environment variables:
- ${REPORTING_SERVICE_API_GET_AVERAGES}
- ${REPORTING_SERVICE_API_GET_AVERAGES_URL}

#### Fields:
Takes the below mandatory RequestParams:
- **unitID**: String - ID of the containing Unit
- **moduleID**: String - module ID
- **timeFrom**: LocalDateTime - The start date and time of the requested report interval (inclusive)
- **timeTo**: LocalDateTime - The end date and time of the requested report interval (exclusive)
- **requestedScopes**: Set of ChronoUnits - A list of requested scopes to be included in the report:
    - **MINUTES**: All the raw values from the module within the given interval 
    - **HOURS**: Hourly averages from the module within the given interval 
    - **DAYS**: Daily averages from the module within the given interval 
    - **MONTHS**: Monthly averages from the module within the given interval
    - **YEARS**: Yearly averages from the module within the given interval 
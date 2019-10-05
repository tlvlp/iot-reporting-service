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

Returns averages within the requested time frame for the requested scopes

#### Related environment variables:
- ${REPORTING_SERVICE_API_GET_AVERAGES}

#### Input:
RequestParams:
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
    
#### Output:
A list of Average objects:
- **scope**: String of ChronoUnit value with the granularity/scope of the average value
- **date**: String form of the date unit of the average (eg. 2019-10 will contain the average of all values in that month)
- **value**: Double form of the calculated average

```
{
    [
        {"scope": "DAYS", "date": "2019-10-06", "value": "12.0"},
        {"scope": "MONTHS", "date": "2019-10", "value": "9.0"},
        {"scope": "YEARS", "date": "2019", "value": "10.0"}
    ]
}
```


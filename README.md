# IoT Server Reporting Service

## Service
Part of the tlvlp IoT project's server side microservices.

This Dockerized SpringBoot-based service is responsible for all reporting related tasks:
- Exposes API for accepting new value entries

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
- **value**: Double - requested value/state of the Module

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
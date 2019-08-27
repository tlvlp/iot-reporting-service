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

### GET XXXXXXXX:

Returns a list of XXXXXXXXXXXX


#### Related environment variables:
- ${XXXXXXXXXXXXXXXXXXX}
- ${XXXXXXXXXXXXXXXXXXX}

#### Fields:
Takes a XXXXXXXXXXXX object in the RequestBody where all the empty fields are ignored
- **XXXXXXXXXXXX**: String - XXXXXXXXXXXX

```
{
    "XXXXXXXXXXXX": "XXXXXXXXXXXX"
}

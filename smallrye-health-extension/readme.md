Keycloak Micro-Profile Health Example
---

Simple extension which demonstrates how to expose Wildfly and Keycloak specific health-checks by leveraging the [MicroProfile Health Subsystem of Wildfly](https://github.com/wildfly/wildfly/blob/master/docs/src/main/asciidoc/_admin-guide/subsystem-configuration/MicroProfile_Health.adoc).
This example uses the API from [eclipse/microprofile-health](https://github.com/eclipse/microprofile-health) backed by [smallrye-health](https://github.com/smallrye/smallrye-health).

The microprofile-health API supports define custom health checks via CDI components which implement the `org.eclipse.microprofile.health.HealthCheck` interface.
Additionally the defined health checks can be grouped into `Readiness` and `Liveness` checks. `Readiness` checks can tell whether the service is ready to 
take requests. `Liveness` checks tell whether the service is still operational.   

# Build
Just build the jar and copy it into the `$KEYCLOAK_HOME/standalone/deployments` folder.
```
mvn clean package
``` 

# Health checks

All health checks can be accessed vua the `/health` endpoint of the wildfly management interface.

## All health checks
http://localhost:9990/health

```json
{
  "status": "UP",
  "checks": [ {
    "name": "keycloak:server",
    "status": "UP",
    "data": {
      "version": "9.0.2",
      "startupTimestamp": 1586550885891
    }
  }, {
  "name": "keycloak:database",
  "status": "UP"
  }]
}
```

## Only liveness checks
http://localhost:9990/health/live
```json
{
  "status": "UP",
  "checks": [ {
  "name": "keycloak:server",
    "status": "UP",
    "data": {
      "version": "9.0.2",
      "startupTimestamp": 1586550885891
    }
  }]
}
```
## Only readiness checks
http://localhost:9990/health/ready
```json
{
  "status": "UP",
  "checks": [ {
      "name": "keycloak:database",
      "status": "UP"
    }]
}
```

# marklogic-geo-data-services

Geo Data Services supporting Esri Koop Provider and WFS (future)

## Feature Service Configuration

Feature services are configured in the ~/feature-services directory. Each feature service should have a corresponding gradle-NAME.properties file where name is the same as the directory under ~/feature-services.
Each feature service must have the services defined and the templates for the feature's data.
A test service is included with this project.

## Installation

Gradle is used to install and configure a feature service into MarkLogic.
Make sure the gradle-NAME.properties file is configured with the correct host, port, and feature service configuration. Additionally, ensure the MarkLogic username and password are set for an administrative user who is able to configure MarkLogic (e.g. admin).

**Install your service**

```
gradlew -PenvironmentName=NAME mlDeploy
gradlew -PenvironmentName=NAME installServices
```

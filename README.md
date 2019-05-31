# marklogic-geo-data-services
MarkLogic Geo Data Services enables feature service data queries against a MarkLogic Server and supports the Esri Koop Provider and WFS (future).

## Gradle Dependency Installation
The MarkLogic Geo Data Services capability can be included in a ml-gradle project using a mlBundle configuration. To add the services to your project you will need to update your build.gradle file, configure a schemas database, add the data services configuration, and add the corresponding indexes. The MarkLogic Geo Data Services modules will be deployed to your modules database when the mlDeploy gradle task is executed.

Please see the _examples_ directory for sample projects making use of this project with ml-gradle's mlBundle capability.

## Variance Plugin
The Variance plugin will be built using the build system's gcc. The MarkLogic CPU architecture and OS must match the build system for the variance plugin to install successfully.

> Note: The native plugin is a C++ program required to support standard deviation and variance aggregation calls for the feature service. gcc is used to build the plugin and then it is installed to MarkLogic via the management API. Because of this _the connector build must run on a machine that is the same platform that the MarkLogic cluster is running on_. If the build is run on a Windows machine, the build and install of the native plugin will be skipped.



### Add the following to your build.gradle file
```
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath "com.marklogic:marklogic-geo-data-services-modules:0.0.4"
  }
}
plugins {
  id "net.saliman.properties" version "1.4.6"
  id "com.marklogic.ml-gradle" version "3.13.0"
}
repositories {
  jcenter()
}

dependencies {
  mlBundle "com.marklogic:marklogic-geo-data-services-modules:0.0.4"
}
```

### Add a schema database to your gradle project
Add the following to your src/main/ml-config/databases/content-database.json

```
  "schema-database": "%%SCHEMAS_DATABASE%%"
```

Add, or create, the following src/main/ml-config/databases/schemas-database.json

```
{
  "database-name": "%%SCHEMAS_DATABASE%%"
}
```

## Configure your data service using the Feature Service Descriptor JSON format
### Feature Service Descriptors
The connector requires a _service descriptor_ document to be created for each ESRI feature service that you want to expose. Service descriptors allow you to define information about service and all of the layers that are available through that service.

Currently, the service descriptors live in the same database as the data that is being queried but that could change. They must be in the `http://marklogic.com/feature-services` collection for the connector to be able to find them.

Service descriptors should be placed in the src/main/ml-data directory. They will be automically installed and placed into the required collection when you run the `gradle mlDeploy` command.

See `examples/sample-project/src/main/ml-data/example/services/GDeltExample.json` for an example service descriptor.

### TDE Templates
The connector relies on MarkLogic TDE views to provide data to ESRI feature layers and tables. Follow the [MarkLogic documentation](https://docs.marklogic.com/guide/app-dev/TDE) and the GKG example provided `src/main/ml-schemas/tde/test-gkg.tdex` to create TDE templates to build views for the data you want to expose.

TDE templates should be placed in the `src/main/ml-schemas/tde` directory. They will be automically installed when you run the `gradle mlDeploy` command.

### Deploy your application
Make use of the `gradle mlDeploy` to deploy your application with MarkLogic Geo Data Services capability.

## Testing this project
1. Configure gradle-test.properties for your environment
2. `gradle -PenvironmentName=test mlDeploy`
3. `gradle -PenvironmentName=test loadTestData`
4. `gradle -PenvironmentName=test test`

## Testing with MarkLogic Koop Provider
You can make use of the koop-provider-marklogic project to test the MarkLogic Geo Data Services though the MarkLogic Koop Connector. The [koop-provider-marklogic](https://github.com/koopjs/koop-provider-marklogic) project expects this repository to be deployed (see: Testing this project), the associated test data, and users & roles to be deployed.
Deploy this application for testing and configure the koop-provider-marklogic to match what was deployed. You can execute the following in two different command line sessions to begin the tests.

#### Command Line 1

1. `gradle installKoop`
2. `gradle runKoop`

#### Command Line 2

1. `gradle -PenvironmentName=test test`

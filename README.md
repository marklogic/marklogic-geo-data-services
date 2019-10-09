# MarkLogic Geo Data Services
MarkLogic Geo Data Services (GDS) enables a rich set of geospatial query capabilities enables against a MarkLogic database. It is the backend behind the [MarkLogic Koop Provider](https://github.com/koopjs/koop-provider-marklogic) and a future release of the [MarkLogic GeoTools Plugin](https://github.com/marklogic-community/marklogic-geotools-plugin).

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=social)](https://opensource.org/licenses/Apache-2.0)

![GitHub release](https://img.shields.io/github/release/marklogic-community/marklogic-geo-data-services.svg?style=social)

![Bintray](https://img.shields.io/bintray/v/prestonmcgowan/maven/marklogic-geo-data-services.svg?style=social)

## Installation
The MarkLogic Geo Data Services capability can be included in a [ml-gradle](https://github.com/marklogic-community/ml-gradle) project using a [mlBundle](https://github.com/marklogic-community/ml-gradle/wiki/Bundles) configuration. To add the services to your project you will need to update your `build.gradle` file, configure a schemas database, add the data services configuration, TDE templates, and add any required indexes. The MarkLogic Geo Data Services modules will be deployed to your modules database when you run the `mlDeploy` gradle task in your project.

Please see the _examples_ directory for sample projects making use of this project with ml-gradle's mlBundle capability.

### Update your build.gradle file
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
If your database doesn't already use a schemas database, you will need to add one to use GDS. To add one, add the following to your `src/main/ml-config/databases/content-database.json`

```
  "schema-database": "%%SCHEMAS_DATABASE%%"
```

And then create, the following `src/main/ml-config/databases/schemas-database.json`

```
{
  "database-name": "%%SCHEMAS_DATABASE%%"
}
```
### Variance Plugin
Geo Data Services relies on a [MarkLogic native plugin](https://docs.marklogic.com/guide/app-dev/native-plugins) to perform standard deviation and variance calculations. The Variance plugin will be built using the build system's gcc. The MarkLogic CPU architecture and OS must match the build system for the variance plugin to install successfully.

> Note: The native plugin is a C++ program required to support standard deviation and variance aggregation calls for the feature service. gcc is used to build the plugin and then it is installed to MarkLogic via the management API. Because of this _the connector build must run on a machine that is the same platform that the MarkLogic cluster is running on_. If the build is run on a Windows machine, the build and install of the native plugin will be skipped.

## Configure your Geo Data Services
### Feature Service Descriptors
GDS uses what it calls a _service descriptor_ to describe the feature services. The service descriptors define the name, description, and other metadata about the services as well what layers are available through that service. Each layer defines the type of features available via the layer as well as the data sources (SPARQL and/or TDE views) that will be used to generate the features.

The service descriptors live in the content database and they must be in the `http://marklogic.com/feature-services` collection for the connector to be able to find them.

To add service descriptors, place them in the `src/main/ml-data` directory of your project along with the correct `collections.properties` and `permissions.properties` files. They will be loaded into your database and placed into the required collection when you run the `./gradlew mlDeploy` task for your project.

See `examples/sample-project/src/main/ml-data/example/services` for example service descriptors and the required `collections.properties` and `permissions.properties` files.

Stay tuned for updates to the wiki for more details about all the configuration options that you can use in the service descriptors.

### TDE Templates
GDS can generate feature data from SPARQL queries, TDE views or a combination of both. To use TDE views, follow the [MarkLogic TDE documentation](https://docs.marklogic.com/guide/app-dev/TDE) and the GKG example provided `examples/sample-project/src/main/ml-schemas/tde/example-gkg.tdex` to create TDE templates to build views for the data you want to expose as features.

TDE templates should be placed in the `src/main/ml-schemas/tde` directory of your project. They will be automically installed when you run the `./gradlew mlDeploy` task for your project.

### _OBJECTIDs_
The features returned by the Koop provider service with an Esri client should contain a field named `OBJECTID` or a field that can be identified as the OBJECTID in to the Esri Feature Service clients. The OBJECTID must be an *unsigned integer*. In order to support pagination across large result sets, the OBJECTIDs need to be increasing numbers. They don't have to be continguous to but should be fairly evenly distributed between the minimum and maximum values.

OBJECTIDs can either be added to the documents and then exposed as a column in a TDE view or computed by an expression in a TDE template column using using existing field(s) in the documents.

For example, you can add the following to each JSON document for a reasonably likely unique id:
```
koopObjectId: xdmp.hash32(sem.uuidString())
```


## Configuring Time Aware Feature Layers

Time aware feature layers allow users to query specific time periods. ArcGIS supports this using a time slider. More info on configuring time settings in ArcGIS Online, https://doc.arcgis.com/en/arcgis-online/create-maps/configure-time.htm. Time aware layers have additional configuration properties, primarily a start and end date. A sample of the layer configuration is included in `src/test/ml-data/feature-services/test/GDeltGKG.json`, layer 6. You must have a dateTime property defined in your TDE. The layer configuration will reference this property name.

### Limitations (TODO in another release)
- This implementation only works for a start date. The end date configured in the layer and in your TDE will be ignored.
- This implementation assumes that all server side dates are in UTC, there are no time zone conversions. The time zone and daylight savings indicator configured in your layer will be ignored.
- The time extent configured in the layer is ignored

## Deploy your application
Once you have added the required configuration to your build file, configured your service descriptors, TDE templates and indexes, use the `./gradlew mlDeploy` task in your project to deploy your application with MarkLogic Geo Data Services.

## Contributing
Geo Data Services is an open source project and we welcome contributions to improve it. Please submit issues for bugs or enhancement requests and, even better, fork it and submit PRs with changes!

### Testing
Use the following steps to install GDS as a standalone project and run the tests:

1. Configure `gradle-test.properties` for your environment
2. Run `./gradlew -PenvironmentName=test mlDeploy`
3. Run `./gradlew -PenvironmentName=test loadTestData`
4. Run `./gradlew -PenvironmentName=test test`

### Testing with MarkLogic Koop Provider
You can make use of the koop-provider-marklogic project to test the MarkLogic Geo Data Services though the MarkLogic Koop Connector. The [koop-provider-marklogic](https://github.com/koopjs/koop-provider-marklogic) project expects this repository to be deployed, the associated test data, and users & roles to be deployed (see: [Testing](#Testing)).
Once that is complete, configure the `gradle-test.properties` in koop-provider-marklogic for your environment and run the following in two different command line sessions to begin the tests.

__Command Line 1__

1. Configure `/config/<environment>.json`
2. Install `npm install`
3. Environment Setting `export NODE_ENV=<environment>`
3. Start Koop `node server.js`

__Command Line 2__

1. `cd test`
2. `../gradlew -PenvironmentName=test test`

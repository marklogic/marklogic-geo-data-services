# marklogic-geo-data-services

Geo Data Services supporting Esri Koop Provider and WFS (future)

## Installation

The MarkLogic Geo Data Services capability can be included in a ml-gradle project using a mlBundle configuration. To add the services to your project you will need to update your build.gradle file, configure a schemas database, add the data services configuration, and add the corresponding indexes. The MarkLogic Geo Data Services modules will be deployed to your modules database when the mlDeploy gradle task is executed.

### Add the following to your build.gradle file
```
buildscript {
  repositories {
    jcenter()
    maven { url "https://dl.bintray.com/prestonmcgowan/maven" }
  }
  dependencies {
    classpath "com.marklogic:marklogic-geo-data-services-modules:0.0.3"
  }
}
plugins {
  id "net.saliman.properties" version "1.4.6"
  id "com.marklogic.ml-gradle" version "3.13.0"
}
repositories {
  jcenter()
  maven { url "https://dl.bintray.com/prestonmcgowan/maven" }
}

dependencies {
  mlBundle "com.marklogic:marklogic-geo-data-services-modules:0.0.3"
}
```
### Add a schema database to your gradle project
Add the following to your src/main/ml-config/databases/content-database.json
```
  "schema-database": "%%SCHEMAS_DATABASE%%"
```
Add, or create,  the following src/main/ml-config/databases/schemas-database.json
```
{
  "database-name": "%%SCHEMAS_DATABASE%%"
}
```

### Configure your data service using the Feature Service Descriptor JSON format

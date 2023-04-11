---
layout: default
title: Tutorial
nav_order: 6
---

This tutorial walks through how to create a new MarkLogic application with GDS installed to enable querying a 
geospatial dataset. If you already have a MarkLogic application and wish to add GDS to it, you should be able to 
use this tutorial as a guide for doing so - just ignore any steps that involve setting up a new project or creating 
files that already exist in your project. 

The finished project can be examined in 
[this example project](https://github.com/marklogic-community/marklogic-geo-data-services/tree/master/examples/arcgis-hub-example).

## Create a project

Create a new project directory with any name you wish; we will assume "arcgis-hub-example" as the dataset used in this 
tutorial is from the [ArcGIS Hub site](https://hub.arcgis.com/search). In that directory, create a file named 
`build.gradle` and add the following to it:

```
plugins {
  id "net.saliman.properties" version "1.5.1"
  id "com.marklogic.ml-gradle" version "4.5.1"
}

repositories {
  mavenCentral()
  maven { url "https://developer.marklogic.com/maven2/" }
}

configurations {
  mlcp
}

dependencies {
  mlBundle "com.marklogic:marklogic-geo-data-services:1.4.0"
  mlcp "com.marklogic:mlcp:10.0.9.5"
  mlcp files("mlcp-logging")
}
```

This sets up an [ml-gradle project](https://github.com/marklogic/ml-gradle) with GDS as a dependency. It also
defines a Gradle configuration named "mlcp" with dependencies that allow
[MarkLogic Content Pump (MLCP)](https://docs.marklogic.com/guide/mlcp/import) to be used to load a geospatial dataset. 

Next, create a file named `gradle.properties` and add the following to it:

```
mlAppName=arcgis-hub-example
mlHost=localhost
mlRestPort=8065
mlUsername=admin
mlPassword=your admin user's password
```

You can choose any app name you want for `mlAppName`. And you can set `mlRestPort` to any available port you want. It 
will be used to create a new [REST API server in MarkLogic](https://docs.marklogic.com/guide/rest-dev/intro).

This application will make use of a [MarkLogic TDE template](https://docs.marklogic.com/guide/app-dev/TDE), and thus
the application must have a schemas database. To add one to the project, create the directory 
`src/main/ml-config/databases` in your project, add a file named `schemas-database.json` to it, and add the following 
content to that file:

```
{
	"database-name": "%%SCHEMAS_DATABASE%%"
}
```

Then, create a file in the same directory named `content-database.json` and add the following content to it:

```
{
  "database-name": "%%DATABASE%%",
  "schema-database": "%%SCHEMAS_DATABASE%%"
}
```

The above files will produce a schemas database in the MarkLogic application, and the content database - which contains
all of an application's data - will use the schemas database to power features such as TDE. 

Finally, the project will use [Gradle](https://gradle.org/) to deploy the application. If you do not have Gradle 
installed, the [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) is recommended as an 
easy way to download a version of Gradle. To do so, copy the following files from the root of this repository to your 
new project:

- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

To verify that you can run Gradle, try the following command:

    ./gradlew tasks

This should print a list of Gradle tasks, including the tasks from the ml-gradle plugin.

## Deploy the application

We'll deploy the application next, which will create a new database in MarkLogic into which a geospatial dataset can 
be loaded. Verify your configuration in `gradle.properties` and then run the following:

    ./gradlew -i mlDeploy

Based on the `gradle.properties` configuration shown above, the following will be created in MarkLogic:

1. An app server named "arcgis-hub-example" on port 8065. 
2. A database named "arcgis-hub-example-content" that includes two required geospatial path indexes.  
3. A database for modules named "arcgis-hub-example-modules"; this contains the GDS modules.
4. A database for schemas named "arcgis-hub-example-schemas".

## Create a user 

While it is reasonable to use a MarkLogic admin user for deploying an application, we want to use a more realistic 
user for accessing GDS endpoints in MarkLogic. Following the [instructions for creating a user](./create-user.md), 
create the file `./src/main/ml-config/security/users/example-user.json` in your project and add the following to it 
(you can alter the password to any value you wish):

```
{
  "user-name": "gds-example-user",
  "password": "password",
  "role": [
    "geo-data-services-writer",
    "rest-extension-user"
  ]
}
```

The GDS `geo-data-services-writer` role allows for this user to access the GDS endpoints in MarkLogic, while the 
MarkLogic `rest-extension-user` allows the user to read the modules in our application's modules database. 

To create this user, run (you may also run `./gradlew mlDeploy` to deploy the entire application at any time):

    ./gradlew mlDeployUsers

You can verify that the user has access to the GDS endpoints by accessing
<http://localhost:8065/v1/resources/modelService> in a web browser, authenticating as the user you just created. The
response will show that no service descriptors, or "models", exist yet:

    {"$version":"1.4.0", "models":{}}

## Load data

We'll next use [MarkLogic Content Pump (MLCP)](https://docs.marklogic.com/guide/mlcp/import) to load a dataset 
containing geospatial data. 

First, go to [this ArcGIS dataset](https://hub.arcgis.com/datasets/NIWA::muricidae-mollusca-gastropoda-from-fiji-and-tonga/explore) 
and click on the download icon - it's the cloud with an arrow pointing down. Click on "Download" for the CSV file and 
save it to `./data/mollusks.csv` (or "molluscs", if you prefer!) in your project directory. This dataset contains 277 
features, each of which has a geospatial point and multiple properties describing the feature. 

Next, add the following to your `build.gradle` file:

```
task loadMollusks(type: com.marklogic.gradle.task.MlcpTask) {
  classpath = configurations.mlcp
  command = "IMPORT"
  port = mlAppConfig.restPort
  database = mlAppConfig.contentDatabaseName
  input_file_path = "data/mollusks.csv"
  input_file_type = "delimited_text"
  delimiter = ","
  document_type = "json"
  uri_id = "id"
  output_collections = "data,mollusks"
  output_permissions = "geo-data-services-reader,read,geo-data-services-writer,update"
  output_uri_replace = ".*/data/,'/'"
  output_uri_prefix = "/data/"
  output_uri_suffix = ".json"
  transform_module = "/mollusks-mlcp-transform.sjs"
}
```

This Gradle task, whose task parameters correspond to
[MLCP import options](https://docs.marklogic.com/guide/mlcp/import#id_23879), will use MLCP to load each of the rows 
in the downloaded CSV file as a new JSON document in the `arcgis-hub-example-content` database. For this to work, we 
need to add a module to the project which is used as a transform by the above MLCP task. Create a file named 
`./src/main/ml-modules/root/mollusks-mlcp-transform.sjs` (create those directories as needed) and add the following 
content to it:

```
'use strict';
function transform(content, context) {
  const doc = content.value.toObject();
  doc.geometry = {
    "type": "Point",
    "coordinates": [parseFloat(doc.X), parseFloat(doc.Y)],
  };
  content.value = doc;
  return content;
}
exports.transform = transform;
```

The geospatial point in each row in the CSV is captured by columns named "X" and "Y". Those values become fields 
in the JSON document created by MLCP. The above transform copies those values into the `geometry` object that GDS 
depends on for geospatial queries. 

After creating this transform, load it into your application:

    ./gradlew mlLoadModules

We can now load the data using MLCP and our transform:

    ./gradlew loadMollusks

The Gradle task logging should show that 277 records were committed to the database - you should see logging similar to 
this:

```
15:25:41.397 [main] INFO  c.m.contentpump.LocalJobRunner - INPUT_RECORDS: 277
15:25:41.397 [main] INFO  c.m.contentpump.LocalJobRunner - OUTPUT_RECORDS: 277
15:25:41.397 [main] INFO  c.m.contentpump.LocalJobRunner - OUTPUT_RECORDS_COMMITTED: 277
15:25:41.397 [main] INFO  c.m.contentpump.LocalJobRunner - OUTPUT_RECORDS_FAILED: 0
15:25:41.397 [main] INFO  c.m.contentpump.LocalJobRunner - Total execution time: 1 sec
```

You can then use [MarkLogic's qconsole](https://docs.marklogic.com/guide/qconsole/intro) to inspect this data that was
loaded into your content database.

## Create a TDE

Following the [instructions for creating a TDE](./create-tde.md), we'll create a TDE template that 
defines the required `OBJECTID` column along with a few other columns to help identify each feature. 

Create the file `./src/main/ml-schemas/tde/mollusks-tde.json` and add the following content to it:

```
{
  "template": {
    "context": "/",
    "collections": [
      "mollusks"
    ],
    "rows": [
      {
        "schemaName": "arcgis_hub",
        "viewName": "mollusks",
        "columns": [
          {
            "name": "OBJECTID",
            "scalarType": "int",
            "val": "id"
          },
          {
            "name": "featureType",
            "scalarType": "string",
            "val": "type"
          },
          {
            "name": "citation",
            "scalarType": "string",
            "val": "bibliographicCitation"
          },
          {
            "name": "latitude",
            "scalarType": "float",
            "val": "Y"
          },
          {
            "name": "longitude",
            "scalarType": "float",
            "val": "X"
          }
        ]
      }
    ]
  }
}
```

Next, load the TDE (you may also run `./gradlew mlDeploy` to deploy the entire application at any time):

    ./gradlew mlLoadSchemas

## Create a service descriptor

Following the [instructions for creating a service descriptor](./create-service-descriptor.md), we'll create a service
descriptor to describe this new feature service with its layer of features based on the mollusk CSV file. 

Create the file `./src/main/ml-data/arcgis-hub-service.json` and add the following content to it:

```
{
  "info": {
    "name": "ArcGIS Hub Data",
    "description": "Contains datasets from https://hub.arcgis.com/ ; each dataset is represented by a layer"
  },
  "layers": [
    {
      "id": 0,
      "name": "Muricidae (Mollusca: Gastropoda) from Fiji and Tonga",
      "description": "Fifty-eight muricid species were collected during recent expeditions to Fiji, including 3 new species.",
      "geometryType": "Point",
      "schema": "arcgis_hub",
      "view": "mollusks",
      "geometry" : {
        "format" : "geojson",
        "coordinateSystem" : "wgs84",
        "xpath" : "//geometry"
      }
    }
  ]
}
```

A critical piece of configuration in the service descriptor is the `geometry` block. That block was added via the 
MLCP transform above when we loaded the mollusk data. GDS uses this configuration to know how to perform geospatial 
queries on the data associated with our TDE, which is identified via the `schema` and `view` fields.

In order for GDS to find our service descriptor, it must be assigned to a specific collection. We also need it to be 
accessible via users with the `geo-data-services-reader` role. To do so, first create the file 
`collections.properties` in the `./src/main/ml-data` directory and add the following content:

    *=http://marklogic.com/feature-services

Next, create the file `permissions.properties` in the same directory and add the following content:

    *=geo-data-services-reader,read,geo-data-services-writer,update

These two files are used by ml-gradle to assign collections and permissions when 
[loading files](https://github.com/marklogic/ml-app-deployer/wiki/Loading-data) in the `ml-data`
directory (the `collections.properties` and `permissions.properties` files are not loaded). 

Finally, load the service descriptor (you may also run `./gradlew mlDeploy` to deploy the entire application at any time):

    ./gradlew mlLoadData

We can now access <http://localhost:8065/v1/resources/modelService> to see that our feature service exists; the response
should be similar to:

```
{
  "$version": "1.4.0",
  "models": {
    "ArcGIS Hub Data": {
      "id": "ArcGIS Hub Data",
      "name": "ArcGIS Hub Data",
      "description": "Contains datasets from https://hub.arcgis.com/ ; each dataset is represented by a layer",
      "layers": [
        {
          "id": 0,
          "name": "Muricidae (Mollusca: Gastropoda) from Fiji and Tonga",
          "description": "Fifty-eight muricid species were collected during recent expeditions to Fiji, including 3 new species.",
          "geometryType": "Point"
        }
      ]
    }
  }
}
```

## Query for data

Run the following `curl` command to query for features that contain the point `178.03333,-18.36667` (this command 
assumes that your application is deployed to port 8065:

```
curl --anyauth --user gds-example-user:password -X POST -H "Content-type: application/json" -d '{"params":{"id":"ArcGIS Hub Data","layer":0,"method":"query"},"query":{"returnGeometry":1,"geometryType":"esriGeometryPoint","spatialRel":"esriSpatialRelContains","extension":{"geometry":{"type":"Point","coordinates":[178.03333,-18.36667]}}}}' http://localhost:8065/v1/resources/geoQueryService
```

You should receive a JSON response containing a `features` array with 5 objects in it. 

Note that both the `modelService` and `geoQueryService` endpoints are not yet considered public in GDS. As noted 
elsewhere in the documentation, it is recommended to interact with GDS via the
[MarkLogic Koop Provider](https://github.com/koopjs/koop-provider-marklogic) or the
[MarkLogic GeoTools Plugin](https://github.com/marklogic-community/marklogic-geotools-plugin).


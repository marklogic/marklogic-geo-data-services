---
layout: default
title: Creating a Service Descriptor
nav_order: 2
---

The geospatial query capabilities provided by GDS depend on service descriptors. A service descriptor is a JSON 
document that defines a feature service and one or more layers. Each layer is defined by a data source, which specifies 
the set of data in MarkLogic that will be queried for that layer. Note that while the terminology and structure are 
very similar to how ArcGIS defines a 
[feature service and layer](https://developers.arcgis.com/rest/services-reference/enterprise/layer-feature-service-.htm),
the intent is not to conform exactly to the ArcGIS definition. 

The documentation below describes how to load and define service descriptors. For a working example, please see the 
example project in the `./examples/sample-project` directory in this repository.


## Loading service descriptors

Each service descriptor must be loaded to the content database in your MarkLogic application. The following approach is 
recommended to accomplish this:

1. Put each service descriptor JSON file in the `src/main/ml-data` directory in your ml-gradle project; you can 
   store these in any child directory that you wish.
2. Add a `collections.properties` file to the directory containing your service descriptor files and add 
`*.json=http://marklogic.com/feature-services` to the file.
3. Add a `permissions.properties` file to the directory containing your service descriptor files and add 
`*.json=rest-reader,read,rest-writer,update` to it, swapping out `rest-reader` and `rest-writer` with 
   application-specific roles if desired. 
4. Run `./gradlew mlLoadData` or `./gradlew mlDeploy` to load the service descriptor files in your content database.

## Defining a service descriptor

A service descriptor is a JSON object that consists of two required top-level keys - `info` and `layers`. Each of these
is described below.

### Info object

The `info` field must be a JSON object containing the following fields:

1. `name` = required; a unique string name for identifying the feature service.
2. `description` = optional string for describing the feature service.

### Layers array

The `layers` field must be an array of JSON objects, one for each layer. Each layer is a JSON object that can 
contain at least the following fields:

1. `id` = required; unique number for identifying the layer.
2. `name` = required; unique string for identifying the layer.
3. `description` = optional string for describing the layer.
4. `geometryType` = required string for identifying type of geometry of each feature. Supported values are `Point` 
   and `Polygon`.
5. `idField` = required string for identifying a column in the associated TDE that contains a value for 
   identifying a feature. 
6. `boundingQuery` = optional JSON object that captures a serialized CTS query which will constrain all queries on 
   this layer for features. 
7. `geometry` = required JSON object that defines the features associated with this layer; defined further below.
8. `dataSources` = array of JSON objects that define queryable data in MarkLogic; defined further below.
9. `schema` = optional string; defined further below.
10. `view` = optional string; defined further below. 

The layer can contain any number of additional fields, which will be included when a client requests a service
descriptor. For example, while GDS does not make use of an `extent` field, a user will typically want to include that
when using the MarkLogic Koop provider so that ArcGIS clients can leverage this field.

#### Data sources

When defining a layer, a user has two choices for defining the source of data to be queried in MarkLogic:

1. Use `schema` and `view` to identify a [TDE template](https://docs.marklogic.com/guide/app-dev/TDE). 
2. Use the `dataSources` array.

The `dataSources` array is intended to provide more flexibility by allowing for multiple sources of data that can be
joined together. Each object in the array can have the following fields:

1. `source` = required string; either `view` or `sparql`.
2. `schema` = required when `source` is `view`; the schema associated with a TDE template.
3. `view` = required when `source` is `view`; the view associated with a TDE template.
4. `query` = required when `source` is `sparql`; a SPARQL query. 
5. `joinOn` = required for the second data source; a JSON object with two keys - `left` and `right` - that 
   identify how data from this data source should be joined with data from the first data source. The `left` key 
   identifies a column in the first data source, while `right` identifies a column in the second data source. The 
   object may optionally have a `joinType` key that defines the type of join; supported values are `inner` (the 
   default), `left outer`, and `full outer`. 
6. `joinFunction` = TODO Don't know yet. 
7. `fields` = required JSON object when `source` is `sparql`; defines fields to add to each feature. Each key in 
   this object is the name of an additional feature field. Each key is an object itself with a
   `scalarType` key that identifies the type of column.
8. `includeFields` = optional array that identifies a subset of the field names to be included in each feature. 
9. `fragmentIdColumn` = optional string for when `source` = `view`; allows for specifying a column to be used for a 
   fragment ID when performing a join. Specifically, this allows for passing in a value for the `systemCols` 
   argument when GDS uses [op.fromView](https://docs.marklogic.com/op.fromView) to build an Optic pipeline based on 
   a view. 

TODO `fields` can also be used when source=view; I'm not sure why though, as it seems GDS can already determine the
fields from the TDE. And "includes" can be used to select a subset of them if desired. 

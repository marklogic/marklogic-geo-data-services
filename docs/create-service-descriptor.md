---
layout: default
title: Create a service descriptor
nav_order: 5
---

The geospatial query capabilities provided by GDS depend on service descriptors (which are also referred to as 
"models"). A service descriptor is a JSON 
document that defines a feature service and one or more layers. Each layer is defined by a data source, which specifies 
the set of data in MarkLogic that will be queried for that layer. Note that while the terminology and structure are 
very similar to how ArcGIS defines a 
[feature service and layer](https://developers.arcgis.com/rest/services-reference/enterprise/layer-feature-service-.htm),
the intent is not to conform exactly to the ArcGIS definition. 

The documentation below describes how to load and define service descriptors. For a working example, please see the 
[example project](https://github.com/marklogic-community/marklogic-geo-data-services/tree/master/examples/sample-project/src/main/ml-data/example/services) 
in this repository.

If you plan on querying data via a MarkLogic TDE template, please read the instructions for 
[creating a TDE](create-tde.md) first. 

* Table of Contents
{:toc}

## Defining a service descriptor

A service descriptor is a JSON object that consists of two required top-level keys - `info` and `layers`. Each of these
is described below.

### Info object

The `info` field must be a JSON object containing the following fields:

1. `name` = required; a unique string name for identifying the feature service.
2. `description` = optional string for describing the feature service.

For example, your initial descriptor file could look like this:

```
{
  "info": {
    "name": "MyService",
    "description": "This is an example of a feature service descriptor file."
  }
}
```


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

### Geometry 

The `geometry` field defines how geometry data is represented in a feature (typically a document in MarkLogic, but 
not restricted to this) so that GDS can query and return this data.  

The value of the field is a JSON object with the following keys:

1. `format` = optional string that defaults to `geojson`; must be one of `geojson`, `gml`, `kml`, `rss`, `mcgm`, 
   `any`, `cts`, or `custom`. 
2. `coordinateSystem` = required string; must be one of the values listed for the 
   [coordinate-system option](https://docs.marklogic.com/geo.geospatialQuery). See
   [the MarkLogic documentation](https://docs.marklogic.com/guide/search-dev/geospatial#id_98149) for more 
   information on coordinate systems.
3. `pointFormat` = only used when `format` is `gml`; string that describes how a point is formatted; must be either 
   `default` (lat/long) or `long-lat-point` (long/lat)
4. `xpath` = optional string, but required if geometry must be returned as part of a query for features; XPath 
   expression that points to the geometry data in a feature. 
5. `source` = optional JSON object used in place of `xpath`; can have the following fields:
   1. `format` = required string; must be one of `geojson`, `wkt`, or `cts`.
   2. `xpath` = optional string; XPath expression that points to the geometry data in a feature.
   3. `column` = optional string; name of a feature column containing the feature's geometry data. 
   4. `documentUriColumn` = optional string; name of a feature column, instead of the internal MarkLogic fragment ID 
      column, used for performing a join when extracting geometry for a feature.
6. `indexes` = optional JSON object that controls how geometry data is queried and extracted. Supports the following 
   child keys:
   1. `regionPath` = optional array of JSON objects for defining a geospatial region query to be used for 
      constraining all queries on the layer. Each JSON object must have a key of `path` and may also have a key of 
      `coordinateSystem`. 
   2. `element` = optional array of JSON objects; for use when `format` is `custom`. Each object must be a 
      serialized MarkLogic element index.
   3. `elementChild` = optional array of JSON objects; for use when `format` is `custom`. Each object must be a
      serialized MarkLogic element child index.
   4. `elementPair` = optional array of JSON objects; for use when `format` is `custom`. Each object must be a
      serialized MarkLogic element pair index.
   5. `elementAttributePair` = optional array of JSON objects; for use when `format` is `custom`. Each object must be a
      serialized MarkLogic element attribute pair index.
   6. `path` = optional array of JSON objects; for use when `format` is `custom`. Each object must be a
      serialized MarkLogic path index.

**For most use cases**, it should suffice to define the `format`, `coordinateSystem`, and `xpath` fields to describe 
a path to the geometry data in a document, where a document is associated with a single feature. 


### Data sources

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
6. `joinFunction` = optional string; one of `joinInner` (default), `joinLeftOuter`, and `joinFullOuter`. Only 
   applied when at least one other data source exists to be joined to the first data source. 
7. `fields` = required JSON object when `source` is `sparql`; defines fields to add to each feature. Each key in 
   this object is the name of an additional feature field. Each key is an object itself with a
   `scalarType` key that identifies the type of column.
8. `includeFields` = optional array that identifies a subset of the field names to be included in each feature. 
9. `fragmentIdColumn` = optional string for when `source` = `view`; allows for specifying a column to be used for a 
   fragment ID when performing a join. Specifically, this allows for passing in a value for the `systemCols` 
   argument when GDS uses [op.fromView](https://docs.marklogic.com/op.fromView) to build an Optic pipeline based on 
   a view. 

## Loading service descriptors

Each service descriptor must be loaded to the content database in your MarkLogic application. The following approach is
recommended to accomplish this:

1. Put each service descriptor JSON file in the `src/main/ml-data` directory in your ml-gradle project; you can
   store these in any child directory that you wish.
2. Add a `collections.properties` file to the directory containing your service descriptor files and add
   `*=http://marklogic.com/feature-services` to the file.
3. Add a `permissions.properties` file to the directory containing your service descriptor files and add
   `*=geo-data-services-reader,read,geo-data-services-writer,update` to it, swapping out `geo-data-services-reader` and 
   `geo-data-services-writer` with application-specific roles if desired.
4. Run `./gradlew mlLoadData` or `./gradlew mlDeploy` to load the service descriptor files in your content database.

## Verifying a service descriptor

After loading your service descriptors into your MarkLogic application's content database, you can verify that they
are accessible via simple requests in your browser and via curl. Note that the URLs below are not yet considered part
of GDS's public interface as directly interacting with GDS is not yet documented nor supported; the expectation is that
clients will use the [MarkLogic Koop provider](https://github.com/koopjs/koop-provider-marklogic) or similar tool 
that depends on GDS. 

The examples below assume that you have installed the 
[example project](https://github.com/marklogic-community/marklogic-geo-data-services/tree/master/examples/sample-project) 
and thus use port 8095. Change this as needed for your own installation of GDS.

Additionally, it is recommended to authenticate as a [user with the GDS roles](create-user.md) as opposed to an admin 
or admin-like user. This user should also have at least read access to the data that can be queried via your service
descriptors' layers. 

To see a list of service descriptors:

<http://localhost:8095/v1/resources/modelService>

To see a particular service descriptor (change the name of the `rs:id` parameter to be that of the service 
descriptor you wish to see):

<http://localhost:8095/v1/resources/modelService?rs:id=GDeltExample>

To query for data, you will need to submit an HTTP POST request, which can be done via a tool like curl. You can use
the statement below, changing `user:password` for the user and password of a user with the [GDS roles](create-user.md):

```bash
curl --anyauth -u user:password -X POST 'localhost:8095/v1/resources/geoQueryService' --header 'Content-Type:application/json' --data-raw '{"params":{"id":"GDeltExample","layer":0,"method":"query"},"query":{"returnCountOnly":true}}'
```

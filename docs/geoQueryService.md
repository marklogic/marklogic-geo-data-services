# GeoQueryService API Documentation

The GeoQueryService provides multiple capabilities:  
 - GeoServer based Queries 
 - Query
 - Export Plan
 - Generate Renderer
 - Generate Layer Descriptor
 - Generate Service Descriptor

## Payload
The Payload for queries coming into GeoQueryService include a params object:
```
{
  "params": {
    "id" : "YourServiceName",
    "layer" : 0
    "method" : "query"
  },
  ... 
}
```

| Field       | Description |
| ----------- | ----------- |
| id          | This is the name of your service, as defined in your service descriptor.   |
| layer       | The layer within yoru service descriptor that you wish to query        |
| method      | The method you wish to invoke on your service |


## Query Method

The query method of the GeoQueryService provides a method to query the data, and returns a geojson repsonse to the caller. The query object includes 3 main top level properties:
| Property | Description | 
| -------- | ----------- |
| returnCountOnly | If `true` this will return the count of objects in the result. |
| returnGeometry  | If `true` this returns the data as geojson, including a populated geometry property.  If `false` the geometry property will be null. | 
| outputStatistics | TBD | 
| extension.geometry | This is an extension point to allow for a single geometry element to constrain a query.  |
| geometryType | Type of geometry.  If it is `ESRIGeometryEnvelope` (lowercased), it performs some ESRI specific modifications to the lat/lon points specified in the extension.geometry object. |
| where | This is either a string of SQL, or an object with a search property at the root.  The SQL passed in must be supported by the Optic/MarkLogic SQL engine. |
| where.search | The Search object includes a structured query as shown in the example below. |
| where.search.qtext | The qtext element within a search object is used to support using the MarkLogic Server Search Grammar as part of the query. | 

### Example Query Object

```
{
  "query" : {
    "returnCountOnly" : false,
    "returnGeometry"  : true,
    "where" : {
      "search" : {
        "qtext" : "Foo OR Bar OR Baz",
        "query" : " {
          "queries" : [
            {
              "geo-elem-pair-query" : {
                "parent" : {"ns":"", "name" : "location"},
                "lat" : {"ns":"", "name" : "latitude"},
                "lon" : {"ns":"", "name" : "longitude"},
                "polygon": [
                  {
                    "point": [
                      { "latitude": 1, "longitude": 2 },
                      { "latitude": 3, "longitude": 4 },
                      { "latitude": 5, "longitude": 6 },
                      { "latitude": 7, "longitude": 8 }
                    ] 
                 } 
                ]
              }
            }
          ]
        }
      }
    }
  }
}
```



## ExportPlan method

The export plan method of the GeoQueryService returns the Optic API Plan for a given query as a JSON object.

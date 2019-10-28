# GeoJSON
GeoJSON geospatial data can be exposed as a feature service through configuration.

Assuming your data looks similiar to the following:

```
{
  "type": "Feature",
  "geometry": {
    "type": "Point",
    "coordinates": [125.6, 10.1]
  },
  "properties": {
    "name": "Dinagat Islands"
  }
}
```
Reference: https://geojson.org/

## Add Geospatial Data Services to your Gradle Project
Create a schema's database for your application.
Add the marklogic-geo-data-services mlBundle. See the project README

```
dependencies {
  mlBundle "com.marklogic:marklogic-geo-data-services-modules:1.1.0"
}
```

## Define your Feature Service
You first define your feature service.

```
{
  "info": {
    "name": "MyGeoData",
    "description": "My GeoJSON data"
  },
  "layers": [
    {
      "id": 0,
      "name": "GeoJSON Data",
      "description": "My data in GeoJSON",
      "geometryType": "Polygon",
      "idField": "OBJECTID",
      "extent": {
        "xmin": -127.923437,
        "ymin": 17.068659,
        "xmax": -63.939062,
        "ymax": 51.242184,
        "spatialReference": {
          "wkid": 4326,
          "latestWkid": 4326
        }
      },
      "schema": "GeoLocation",
      "view": "GeoJSON",
      "geometry": {
        "type": "Polygon",
        "format": "geojson",
        "coordinateSystem": "wgs84",
        "source": {
          "xpath": "/envelope/instance/boundary"
        }
      }
    }
  ]
}
```

A few notes on the Feature Service configuration:

* You can have multiple layers, each layer must have a unique incrementing `id`
* If you are supporting Esri products, uou must identify an idField with a unique number for each document's data points. See the README for more information.
* The Schema and View will identify your TDE

## Define your Template (TDE)
The Template Driven Extraction (TDE)

## Deploy your application
Using mlGradle, deploy to your MarkLogic system.

```
gradle mlDeploy -PenvironmentName=local
```

## Ingest your data

## Test your data


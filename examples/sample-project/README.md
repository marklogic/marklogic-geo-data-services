# Sample Project (marklogic-geo-data-services)
The intent of this sample project is to provide a basic layout using the ml-gradle (3.13.0) directory layout and to include the marklogic-geo-data-services project as an mlBundle.

## Deploying
To deploy this project, configure gradle.properties for your environment and run `gradle mlDeploy`. The mlDeploy will take care of deploying the MarkLogic database configuration, users, roles, data templates, REST Services, and feature-service configuration. The test data is located under the data directory and is loaded to MarkLogic with the following command, `gradle loadExampleData`.

## Accessing
The configured feature-services are available at the following REST paths:

### top-level service descriptor
```
GET http://localhost:8040/LATEST/resources/KoopSearchServices
Content-type: application/json
Authorization: Digest admin admin
```

### layer 0 descriptor:
```
POST http://localhost:8040/LATEST/resources/geoQueryService
Content-Type: application/json
Authorization: Digest admin admin

{
    "params": {
        "id": "GDeltExample",
        "layer": 0
    }
}
```

### query layer 0 for the first 5 features:
```
POST http://localhost:8040/LATEST/resources/geoQueryService
Content-Type: application/json
Authorization: Digest admin admin

{
  "params": { 
    "id": "GDeltExample",
    "layer": "0",
    "method": "query" 
  },
  "query": { 
    "resultRecordCount": 5,
    "orderByFields": "name",
    "nbspASC": 0,
    "returnGeometry": true
  }
}
```

### query layer 0 for the first 5 features without geometry:
```
POST http://localhost:8040/LATEST/resources/geoQueryService
Content-Type: application/json
Authorization: Digest admin admin

{
  "params": { 
    "id": "GDeltExample",
    "layer": "0",
    "method": "query" 
  },
  "query": { 
    "resultRecordCount": 5,
    "orderByFields": "name",
    "nbspASC": 0,
    "returnGeometry": false
  }
}
```
### query layer 0 for the count of features:
```
POST http://localhost:8040/LATEST/resources/geoQueryService
Content-Type: application/json
Authorization: Digest admin admin

{
  "params": { 
    "id": "GDeltExample",
    "layer": "0",
    "method": "query" 
  },
  "query": { 
    "resultRecordCount": 5,
    "nbspASC": 0,
    "returnCountOnly": true
  }
}
```
### query layer 0 for the features where the "domain" is "indiatimes.com": 

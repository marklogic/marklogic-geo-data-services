The intent of this project is to both demonstrate how a ml-gradle-based project can depend on 
marklogic-geo-data-services via the `mlBundle` Gradle configuration and also to facilitate manual testing during 
development. 

## Deploying

To deploy this project, do the following:

1. Follow the instructions in this repository's CONTRIBUTING.md file for publishing marklogic-geo-data-services to 
   your local Maven repository. 
2. In this project directory, Create `gradle-local.properties` and set `mlUsername` and `mlPassword` to your admin or 
   admin-like username and password.
3. Run `./gradlew -i downloadExampleData mlDeploy loadExampleData` . 

Note that after downloading and loading the example data, you can run `mlDeploy` by itself whenever you want to update
the application deployed by this project.

## Testing

To test this project, [install Postman](https://www.postman.com/downloads/) and then import the 
`sample-project-tests.postman_collection.json` file in this project directory. You'll need to create a workspace first 
so that you can import the file. You can then run each of the requests in the collection.


# TODO Replace everything below with more requests in the Postman collection


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

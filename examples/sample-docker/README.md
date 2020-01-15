# Sample Esri Connector Stack using Docker Compose

This is an example on how to use Docker Compose to stand up a working Esri Connector environment that contains:
- A MarkLogic server with MarkLogic Geo Data Services installed
- A Koop server with the MarkLogic provider plugin installed
- Database populated with example data (GDelt and Zip Codes)

## Prerequisites

1. Docker

## Quick Start

1. Open a new terminal and run `docker-compose up` or `docker-compose -d up` if detached mode is preferred.  Standing up for the first time will take several minutes as it will pull images, code, and sample data from various repositories.
2. To check if the environment is up, open a browser and navigate to `http:localhost:9000/marklogic/GDeltExample/FeatureServer` and see if it returns a JSON response.
3. To check if the sample data has been loaded, open a browser and navigate to `http://localhost:9000/marklogic/GDeltExample/FeatureServer/0/query?where=1=1&resultRecordCount=10`.  This should return a JSON response with some listed features.
4. When you are finished with the environment, run `docker-compose down`.

## Defaults
- MarkLogic Admin page: http://localhost:10001
- MarkLogic QConsole page: http://localhost:10000/qconsole
- Koop service: http://localhost:9000

## Details

The environment should consist of two running (2) containers: `ml` and `koop`.  A 3rd container named `deploy_gds` is also present and is used to configure and deploy the `sample-project` project (located in the `examples` folder), as well as download and populate the database with sample data (GDelt and zip codes).  It will exit once done.


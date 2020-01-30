# Sample Esri Connector Stack using Docker Compose

This is an example on how to use Docker Compose to stand up a working Esri Connector environment that contains:

- A MarkLogic server with the Geo Data Services installed
- A [Koop](https://github.com/koopjs/koop-provider-marklogic) server with the MarkLogic provider plugin installed
- Database populated with example data (GDelt and Zip Codes)

## Prerequisites

1. [Docker](https://www.docker.com/products/docker-desktop)

## Quick Start

1. Open a new terminal.
2. Run `docker-compose build` to build the **koop** and **deployer** container images.
3. Run `docker-compose up` (or `docker-compose -d up` if you wish to run in detached mode).  Note that standing up the environment for the first time will take several minutes as it will pull container images and sample data.
4. Wait for the **deployer** container to exit.  This can be seen in the terminal if you ran in attached mode.  You can also run `docker ps` to check if the container is still running.T
5. Open a browser and head to this [URL](http://localhost:9000/marklogic/GDeltExample/FeatureServer/0/query?where=1=1&resultRecordCount=10) to check if the **koop** service is running and whether the example data has been loaded.
6. When you are finished with the environment, run `docker-compose down`.

## Defaults

- [MarkLogic Admin UI](http://localhost:10001) at port **10001**
- [MarkLogic QConsole](http://localhost:10000/qconsole) at port **10000**
- [Koop server](http://localhost:9000) at port **9000**

Ports can be changed in `docker-compose.yml`.

## Details

The environment should have two running (2) containers: **ml** and **koop**.  A 3rd container named **deployer** will also run during `docker-compose up` to deploy the **sample-project** project (located in the `examples` folder) and will exit once done.

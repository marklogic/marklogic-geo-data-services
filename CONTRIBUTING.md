This guide provides instructions on developing and testing changes to this project. 

# Running the automated tests

Use the following steps to install GDS as a standalone project and run the tests:

1. Create `gradle-local.properties` and set `mlUsername` and `mlPassword` to your admin or admin-like username and 
   password
2. Run `./gradlew -i mlDeploy loadTestData test`

# Manual testing

You can perform manual testing by following the instructions in the `examples/sample-project/README.md` file for 
deploying an application that depends on marklogic-geo-data-services. 

Before doing so, you'll first need to publish marklogic-geo-data-services locally. To do so, run:

    ./gradlew publishToMavenLocal

This will put a copy of the marklogic-geo-data-services library in your local Maven repository (which defaults to 
`~/m2/repository`). This is required so that the sample project can depend on marklogic-geo-data-services via the 
[ml-gradle mlBundle Gradle configuration](https://github.com/marklogic-community/ml-gradle/wiki/Bundles).


# Testing with MarkLogic Koop Provider

TODO This will be updated and verified soon as part of 1.4.0.

You can make use of the koop-provider-marklogic project to test the MarkLogic Geo Data Services though the MarkLogic Koop Connector. The [koop-provider-marklogic](https://github.com/koopjs/koop-provider-marklogic) project expects this repository to be deployed, the associated test data, and users & roles to be deployed (see: [Testing](#Testing)).
Once that is complete, configure the `gradle-test.properties` in koop-provider-marklogic for your environment and run the following in two different command line sessions to begin the tests.

#### __Command Line 1__

1. Configure `/config/<environment>.json`
2. Install `npm install`
3. Environment Setting `export NODE_ENV=<environment>`
4. Start Koop `node server.js`

#### __Command Line 2__

1. `cd test`
2. `../gradlew -PenvironmentName=test test`

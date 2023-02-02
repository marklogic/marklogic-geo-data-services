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

Clone the https://github.com/koopjs/koop-provider-marklogic repository, checkout the develop branch, and read the 
CONTRIBUTING.md file.

---
layout: default
title: Install and Upgrade
nav_order: 2
---

## System Requirements

To use GDS, MarkLogic 10 or higher is required, with the latest version of MarkLogic being preferred to take 
advantage of updates to the Optic engine in MarkLogic.

For deploying your MarkLogic application with GDS bundled inside it, please see 
[the ml-gradle documentation](https://github.com/marklogic/ml-gradle) on which version of Gradle to use. 

## Installing GDS

The GDS modules can be added to a MarkLogic application that uses
[ml-gradle](https://github.com/marklogic-community/ml-gradle) by declaring an
[mlBundle](https://github.com/marklogic-community/ml-gradle/wiki/Bundles) configuration:

    dependencies {
      mlBundle "com.marklogic:marklogic-geo-data-services:1.4.0"
    }

When you run the Gradle `mlDeploy`, ml-gradle will download the GDS artifact and install the modules within it into 
your application's modules database. In addition, the GDS bundle includes two required geospatial indexes for your 
content database along with several security resources, including amps, a protected collection, and roles. These 
resources will also be installed when running `mlDeploy`.

> Geo Data Services relies on a [MarkLogic native plugin](https://docs.marklogic.com/guide/app-dev/native-plugins) to
> perform standard deviation and variance calculations. The plugin is built using the build system's gcc. The MarkLogic 
> CPU architecture and OS must match the build system for the variance plugin to install successfully. If you run into
> installation problems and do not need standard deviation and variance calculations, you can disable installation of 
> the plugin by adding `mlPluginInstallationEnabled=false` to your `gradle.properties` file.

### Loading data

After installing GDS, you may have a need for loading data if you have not already done so in your application. 
Geospatial datasets can often be obtained as files. If you need to load data from files, consider using 
[MarkLogic Content Pump (MLCP)](https://docs.marklogic.com/guide/mlcp/import) for this. You may also find it 
convenient to 
[run MLCP via Gradle](https://github.com/marklogic/ml-gradle/wiki/MarkLogic-Content-Pump-(mlcp)-and-Gradle).  

## Upgrading GDS

If you have already added GDS to your MarkLogic application and wish to upgrade, simply change the version number
of the GDS dependency in your build.gradle file. Then run the Gradle `mlDeploy` task to deploy your application with 
the upgraded version of GDS.

Additionally, be sure to read 
[the release notes](https://github.com/marklogic/marklogic-geo-data-services/releases) when you upgrade, 
particularly to see if any changes should be made to your service descriptors. 


---
layout: default
title: Install and Upgrade
nav_order: 2
---

## Installing GDS

The GDS modules can be added to a MarkLogic application that uses
[ml-gradle](https://github.com/marklogic-community/ml-gradle) by declaring an
[mlBundle](https://github.com/marklogic-community/ml-gradle/wiki/Bundles) configuration:

    dependencies {
      mlBundle "com.marklogic:marklogic-geo-data-services:1.4.0"
    }

When you run the Gradle `mlDeploy` or `mlLoadModules` tasks, ml-gradle will download the GDS artifact and install
the modules within it into your application's modules database.

> Geo Data Services relies on a [MarkLogic native plugin](https://docs.marklogic.com/guide/app-dev/native-plugins) to
> perform standard deviation and variance calculations. The plugin is built using the build system's gcc. The MarkLogic 
> CPU architecture and OS must match the build system for the variance plugin to install successfully. If you run into
> installation problems and do not need standard deviation and variance calculations, you can disable installation of 
> the plugin by adding `mlPluginInstallationEnabled=false` to your `gradle.properties` file.


## Upgrading GDS

If you have already added GDS to your MarkLogic application and wish to upgrade, simply change the version number
of the GDS dependency in your build.gradle file. 

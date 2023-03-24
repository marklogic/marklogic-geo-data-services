---
layout: default
title: Create a TDE template
nav_order: 4
---

When querying for data in a layer defined by a service descriptor, GDS can use either a TDE template or a SPARQL query
as a source of data. If you only intend to use SPARQL queries, then you do not need to follow any of the instructions 
in this section. 


## Creating a TDE template

The [MarkLogic docs for TDE templates](https://docs.marklogic.com/guide/app-dev/TDE) will help you with creating a 
template. It is often easier though to start with an existing template and customize it. To do so, you can use  
[this example TDE file](https://github.com/marklogic-community/marklogic-geo-data-services/blob/master/examples/sample-project/src/main/ml-schemas/tde/example-gkg.xml) 
as a starting point. 

A requirement for your TDE is that it must either declare a column named "OBJECTID" or it must declare a column with 
a name that corresponds to the `idField` in [a service descriptor](create-service-descriptor.md). In either case, the 
column must contain unsigned integers in order to work properly as an ArcGIS Feature Service. Additionally, to support 
pagination across large result sets, the values do not need to be contiguous but should be fairly even distributed 
between the minimum and maximum values. 

If the rows projected by your TDE do not have a column already that contains unsigned integers, you could use a data
transformation tool like [CoRB](https://developer.marklogic.com/code/corb/) to add something similar to this to each 
document (this example would be for JSON documents):

    objectId: xdmp.hash32(sem.uuidString())

Once you have an identifier column established, you are free to declare any other columns you wish based on the 
properties you wish to expose in each feature. You will likely want to declare at least one column that contains values
that act as human-friendly labels for the features. That column can be used as the `displayField` for a layer when you 
[create a service descriptor](./create-service-descriptor.md).

### Invalid column names

The column name `type` is not supported when performing SQL queries on that column. 

## Loading TDE templates

TDE templates can be loaded by placing them in the `src/main/ml-schemas/tde` directory in an 
[ml-gradle project](https://github.com/marklogic/ml-gradle/wiki/Loading-schemas).

Before loading them, you will first need to ensure that your content database is associated with a schema database. 
For a typical MarkLogic application, a custom schemas database should be used instead of the default "Schemas" database
in MarkLogic. To achieve this in an ml-gradle project, add the following to your content database file, which defaults 
to `src/main/ml-config/databases/content-database.json`:

    "schema-database": "%%SCHEMAS_DATABASE%%"

Then, create a `src/main/ml-config/databases/schemas-database.json` file with the following content:

```
{
  "database-name": "%%SCHEMAS_DATABASE%%"
}
```

Then run the following to deploy (or redeploy) your application, which will create the schemas database and load your 
TDE's:

    ./gradlew -i mlDeploy

---
layout: default
title: Create a MarkLogic user
nav_order: 3
---

After [installing GDS](install.md), the following roles will exist in your MarkLogic server:

1. `geo-data-services-reader` provides read-only access to service descriptors.
2. `geo-data-services-writer` provides full control over service descriptors. 

It is recommended to create a MarkLogic user with the above roles along with the MarkLogic `rest-extension-user` role. 
That user can then be used when accessing GDS endpoints. For actions such as [loading a TDE](create-tde.md) and 
[loading a service descriptor](create-service-descriptor.md), you should typically use an admin or admin-like user as 
those actions are performed during the application deployment process.

If you are using ml-gradle, you can 
[define this user as a JSON file](https://github.com/marklogic/ml-gradle/wiki/Resource-reference#security---users) in 
your project's `src/main/ml-config/security/users` directory and deploy it either via `mlDeployUsers` or `mlDeploy`.

Note that the above roles only control access to the GDS endpoints. They do not control which documents a user can query
and update. Those operations are secured via standard MarkLogic document permissions.

/*
* Copyright © 2017-2019 MarkLogic Corporation
*/

'use strict';

/*
 * geoserverProfile should be a JSON object that contains everything needed to connect as an admin to the geoserver instance:
 * {
 *   "url": "http://localhost:8080/geoserver",
 *   "workspace" : "marklogic",
 *   "datastore" : "mlstore",
 *   "auth" : {
 *               "method" : "basic",
 *               "username" : "admin",
 *               "password" : "geoserver"
 *            }
 * }
 */

function deleteLayersInGroup(geoserverUrl, workspace, authenticationJson, serviceDescriptorURI) {
  const httpOptions = {
    "authentication" : authenticationJson,
    "headers" : {
      "content-type":"application/json"
    }
  }
  const serviceDescriptor = cts.doc(serviceDescriptorURI).toObject();

  xdmp.trace("GEOSERVER-DEBUG", "Found descriptor to delete: " + serviceDescriptor.info.name);
  if (serviceDescriptor) {
    for (const layer of serviceDescriptor.layers) {
      xdmp.trace("GEOSERVER-DEBUG", "Deleting layer id: " + layer.geoServerMetadata.geoServerLayerName);
      checkResponse(xdmp.httpDelete(geoserverUrl+"\/rest\/layers\/"+workspace+":"+layer.geoServerMetadata.geoServerLayerName + "?recurse=true", httpOptions)).toObject()[1];
    }
  }
  else {
    fn.error(null, 'RESTAPI-SRVEXERR', "No Service Descriptor found at " + serviceDescriptorURI);
  }
}

function publishLayersInGroup(geoserverUrl, workspace, datastore, authenticationJson, serviceDescriptorURI) {
  const restNewFeatureType = "\/rest\/workspaces\/"+workspace+"\/datastores\/"+datastore+"\/featuretypes.json"
  const httpOptions = {
    "authentication" : authenticationJson,
    "headers" : {
      "content-type":"application/json"
    }
  }

  const serviceDescriptor = cts.doc(serviceDescriptorURI).toObject();

  if (serviceDescriptor) {
    let published = [];
    let layerGroupBounds = {
      "minx": 0,
      "maxx": 0,
      "miny": 0,
      "maxy": 0,
      "crs": "EPSG:4326"
    }
    for (const layer of serviceDescriptor.layers) {
      const json = generateLayerJson(layer,workspace,datastore,geoserverUrl);
      xdmp.trace("GEOSERVER-DEBUG", "Trying... " + geoserverUrl+restNewFeatureType);
      const response = checkResponse(xdmp.httpPost(geoserverUrl+restNewFeatureType, httpOptions, json));
      xdmp.trace("GEOSERVER-DEBUG", "Response was: " + response);

      // Dynamically generating the layergroup bounds... maybe correctly?
      if (layerGroupBounds.minx > layer.extent.xmin) { layerGroupBounds.minx = layer.extent.xmin;}
      if (layerGroupBounds.miny > layer.extent.ymin) { layerGroupBounds.miny = layer.extent.ymin;}
      if (layerGroupBounds.maxx < layer.extent.xmax) { layerGroupBounds.maxx = layer.extent.xmax;}
      if (layerGroupBounds.maxy < layer.extent.ymax) { layerGroupBounds.maxy = layer.extent.ymax;}
      if (layer.extent.spatialReference && layer.extent.spatialReference.wkid) {
        layerGroupBounds.crs = "EPSG:" + layer.extent.spatialReference.wkid;
      }

      xdmp.trace("GEOSERVER-DEBUG", "Checking published status, trying... " + geoserverUrl+"\/rest\/layers\/"+workspace+":"+layer.geoServerMetadata.geoServerLayerName+".json");
      published.push(checkResponse(xdmp.httpGet(geoserverUrl+"\/rest\/layers\/"+workspace+":"+layer.geoServerMetadata.geoServerLayerName+".json", httpOptions)).toObject()[1])
    }

    const lgJson = generateLayerGroupJson(workspace,serviceDescriptor.info, published, layerGroupBounds);
    const groupResponse = checkResponse(xdmp.httpPost(geoserverUrl+"\/rest\/layergroups", httpOptions, lgJson)).toObject();
    xdmp.trace("GEOSERVER-DEBUG", "Group Response: " + groupResponse);
    return groupResponse;


  } else {
    fn.error(null, 'RESTAPI-SRVEXERR', "No Service Descriptor found at " + serviceDescriptorURI);
  }
}

function checkResponse(response) {
  let code = response.toObject()[0].code
  if (code > 299 || code < 200) {
    fn.error(null, 'RESTAPI-SRVEXERR', response);
  } else {
    return response;
  }
}

function generateLayerGroupJson(workspace, layerInfo, published, bounds) {
  const template = {
    "layerGroup": {
      "name": layerInfo.name,
      "title": layerInfo.name,
      "abstractTxt": layerInfo.description,
      "workspace": {
        "name": workspace
      },
      "publishables": {
        "published": []
      },
      "styles": {
        "style": []
      },
      "bounds": bounds
    }
  }

  xdmp.trace("GEOSERVER-DEBUG", "Bounds: " + template.layerGroup.bounds);

  for (const player of published) {

    xdmp.trace("GEOSERVER-DEBUG", "Published: " + player);

    const layer = player.toObject().layer
    let publishDetails =  {
      "@type":"layer",
      "name" : layer.resource.name,
      "href" : layer.resource.href
    }
    template.layerGroup.publishables.published.push(publishDetails)
    let styleDetails;
    if (layer.defaultStyle) {
      styleDetails = {
        "name" : layer.defaultStyle.name,
        "href" : layer.defaultStyle.href
      }
    } else {
      styleDetails = null
    }
    template.layerGroup.styles.style.push(styleDetails)
  }
  return template;
}

function generateLayerJson(layer, workspace, datastore, geoserverUrl) {
  //TODO: VERIFY ATTRIBUTES ARE CORRECT
  return {
    "featureType": {
      "name": layer.geoServerMetadata.geoServerLayerName,
      "nativeName": layer.geoServerMetadata.geoServerLayerName,
      "title": layer.name,
      "abstract": layer.name + ": " + layer.description,
      "srs": layer.geoServerMetadata.coordinateSystem || "EPSG:4326",
      "nativeBoundingBox": {
        "minx": layer.extent.xmin,
        "maxx": layer.extent.xmax,
        "miny": layer.extent.ymin,
        "maxy": layer.extent.ymax,
        "crs": layer.geoServerMetadata.coordinateSystem || "EPSG:4326"
      },
      "latLonBoundingBox": {
        "minx": layer.extent.xmin,
        "maxx": layer.extent.xmax,
        "miny": layer.extent.ymin,
        "maxy": layer.extent.ymax,
        "crs": layer.geoServerMetadata.coordinateSystem || "EPSG:4326"
      },
      "projectionPolicy": "NONE",
      "enabled": true,
      "store": {
        "@class": "dataStore",
        "name": workspace + ":" + datastore,
        "href": geoserverUrl + "\/rest\/workspaces\/" + workspace + "\/datastores\/" + datastore + ".json"
      },
      "serviceConfiguration": false,
      "maxFeatures": 0,
      "numDecimals": 0,
      "padWithZeros": false,
      "forcedDecimal": false,
      "overridingServiceSRS": false,
      "skipNumberMatched": false,
      "circularArcPresent": false
    }
  }
}

exports.geoserverPublisher = publishLayersInGroup
exports.geoserverUnpublisher = deleteLayersInGroup

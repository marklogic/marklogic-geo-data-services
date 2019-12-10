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


function publishLayersInGroup(geoserverProfile, serviceDescriptorURI) {
    const workspace = geoserverProfile.workspace;
    const datastore = geoserverProfile.datastore;
    const geoserverUrl = geoserverProfile.url;
    const restNewFeatureType = "\/rest\/workspaces\/"+workspace+"\/datastores\/"+datastore+"\/featuretypes.json"
    const httpOptions = {
        "authentication" : geoserverProfile.auth,
        "headers" : { "content-type":"application/json"}
    }

    const serviceDescriptor = cts.doc(serviceDescriptorURI).toObject();

    if (serviceDescriptor) {
        let published = [];
        for (const layer of serviceDescriptor.layers) {
            const json = generateLayerJson(layer,workspace,datastore,geoserverUrl);
            xdmp.httpPost(geoserverUrl+restNewFeatureType, httpOptions, json)
            published.push(xdmp.httpGet(geoserverUrl+"\/rest\/layers\/"+workspace+":"+layer.geoServerMetadata.geoServerLayerName+".json", httpOptions).toObject()[1])
        }

        const lgJson = generateLayerGroupJson(workspace,serviceDescriptor.info, published)

        return xdmp.httpPost(geoserverUrl+"\/rest\/layergroups", httpOptions, lgJson)

    } else {
        fn.error("No Service Descriptor found at " + serviceDescriptorURI);
    }
}

function generateLayerGroupJson(workspace, layerInfo, published) {
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
            "bounds": {
                "minx": -180,
                "maxx": 180,
                "miny": -90,
                "maxy": 90,
                "crs": "EPSG:4326"
            }
        }
    }

    for (const player of published) {
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
    return {
        "featureType": {
            "name": layer.geoServerMetadata.geoServerLayerName,
            "nativeName": layer.geoServerMetadata.geoServerLayerName,
            "title": layer.description,
            "abstract": layer.name + ": " + layer.description,
            "srs": "EPSG:4326",
            "nativeBoundingBox": {
                "minx": layer.extent.xmin,
                "maxx": layer.extent.xmin,
                "miny": layer.extent.ymin,
                "maxy": layer.extent.ymax,
                "crs": "EPSG:4326"
            },
            "latLonBoundingBox": {
                "minx": -180,
                "maxx": 180,
                "miny": -90,
                "maxy": 90,
                "crs": "EPSG:4326"
            },
            "projectionPolicy": "FORCE_DECLARED",
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
            "circularArcPresent": false,
            "attributes": {
                "attribute": [
                    {
                        "name": "geometry",
                        "minOccurs": 0,
                        "maxOccurs": 1,
                        "nillable": true,
                        "binding": "org.locationtech.jts.geom.Point"
                    }
                ]
            }
        }
    }
}

exports.geoserverPublisher = publishLayersInGroup
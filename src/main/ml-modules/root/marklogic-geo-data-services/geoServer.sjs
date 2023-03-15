'use strict';

/**
 * Supports requests from the marklogic-geotools-plugin module.
 * See https://geoserver.org/ for more information on GeoServer.
 *
 * Unfortunately there's a good chunk of GeoServer-specific logic in serviceLib that will take quite a bit of
 * doing to extricate into this module.
 */

const serviceLib = require('/marklogic-geo-data-services/serviceLib.sjs');

function getGeoServerData(req) {
  if (req.geoserver.method === "getLayerNames") {
    return getGeoServerLayerNames();
  }
  else if (req.geoserver.method == "getLayerSchema") {
    const layerName = req.geoserver.layerName;
    const descriptor = findServiceDescriptor(layerName);
    return serviceLib.getGeoServerLayerSchema(layerName, descriptor);
  }
}

function findServiceDescriptor(geoServerLayerName) {
  const descriptor = fn.head(cts.search(
    cts.andQuery([
      cts.collectionQuery(serviceLib.SERVICE_DESCRIPTOR_COLLECTION),
      cts.jsonPropertyValueQuery("geoServerLayerName", geoServerLayerName)
    ])
  ));
  if (!descriptor) {
    throw "Layer info for " + geoServerLayerName + " not found";
  }
  return descriptor;
}

function getGeoServerLayerNames() {
  const layerNames = [];
  for (let descriptorDoc of cts.search(
    cts.andQuery([
      cts.collectionQuery(serviceLib.SERVICE_DESCRIPTOR_COLLECTION),
      cts.jsonPropertyScopeQuery("geoServerLayerName", cts.trueQuery())
    ])
  )) {
    for (let layer of descriptorDoc.toObject().layers) {
      if (layer.geoServerMetadata && layer.geoServerMetadata.geoServerLayerName)
        layerNames.push(layer.geoServerMetadata.geoServerLayerName);
    }
  }
  return layerNames;
}

module.exports = {
  getGeoServerData
}

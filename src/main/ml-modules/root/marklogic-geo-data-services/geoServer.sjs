/*
 * Copyright (c) 2023 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

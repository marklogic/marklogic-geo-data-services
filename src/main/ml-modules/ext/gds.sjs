'use strict';

function getServiceModel(serviceName, geoServerLayerName = null) {
  xdmp.trace("KOOP-DEBUG", "Starting getServiceModel");
  // TODO: These should be cached

  const collection = "http://marklogic.com/feature-services";

  let propertyNames = ["name"];
  if (geoServerLayerName) propertyNames.push("geoServerLayerName");

  xdmp.trace("KOOP-DEBUG", "Searching for Service Model: " + serviceName);
  let model = fn.head(
      cts.search(cts.andQuery([
        cts.collectionQuery(collection),
        cts.jsonPropertyValueQuery(propertyNames, [serviceName, geoServerLayerName])
      ]))
  );

  if (model) {
    xdmp.trace("KOOP-DEBUG", "Found service: " + serviceName);
    return model.toObject();
  } else {
    xdmp.trace("KOOP-DEBUG", "No service info found for: " + serviceName);
    throw "No service info found for: " + serviceName;
  }
}

exports.getServiceModel = getServiceModel;
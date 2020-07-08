'use strict';
const err = require('/ext/error.sjs');
const trace = require('/ext/trace.sjs');

const MODEL_CACHE_NAME = 'http://marklogic.com/feature-services/model-cache';
const SERVICE_DESCRIPTOR_COLLECTION = 'http://marklogic.com/feature-services';

function getModelCache() {
  let seq = xdmp.getServerField(MODEL_CACHE_NAME); // returns a sequence
  if (!fn.exists(seq)) {
    seq = xdmp.setServerField(MODEL_CACHE_NAME, Sequence.from({}));
  }
  return seq.toObject();
}

function setModelCache(modelInfos, replace = false) {
  const modelCache = replace ? {} : xdmp.getServerField(MODEL_CACHE_NAME);
  modelInfos.forEach(modelInfo => modelCache[modelInfo.serviceId] = modelInfo);
  xdmp.setServerField(MODEL_CACHE_NAME, Sequence.from(modelCache));
}

function getServiceModelInfoFromCache(serviceId) {
  const modelCache = getModelCache();
  return modelCache[serviceId];
}

function getServiceModelInfoFromDb(serviceId, cacheAfterLoad = true) {
  const modelDoc = fn.head(
    cts.search(cts.andQuery([
      cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION),
      cts.jsonPropertyValueQuery("name", serviceId, ["exact"])
    ]))
  );
  const model = modelDoc ? modelDoc.toObject() : null;
  const modelInfo = {
    serviceId: model.info.name,
    model: model,
    uri: fn.baseUri(modelDoc)
  };
  if (cacheAfterLoad && model) {
    setModelCache([modelInfo]);
  }
  return modelInfo;
}

function getServiceModels(filter) {
  const validFilters = new Set(['all', 'search', 'geoserver']);
  const _filter = filter || 'all';
  if (!validFilters.has(_filter)) { throw err.newInputError(`Invalid filter '${_filter}'.`)}

  // (re)select all descriptors and cache them
  const allModelInfos = cts.search(cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION))
    .toArray()
    .map(doc => { 
      const model = doc.toObject();
      return {
        serviceId: model.info.name,
        model: model,
        uri: fn.baseUri(doc)
      };
    });
  setModelCache(allModelInfos, true);

  trace.info(`Found a total of ${allModelInfos.length} service descriptor documents.`, "getServiceModels");
  const allModels = allModelInfos.map(m => m.model);
  if (_filter === 'search')
    return allModels.filter(m => m.search);
  else if (_filter === 'geoserver')
    return allModels.filter(m => m.layers && Array.isArray(m.layers) && m.layers.some(l => l.geoServerMetadata));
  else
    return allModels;
}

function getServiceModelInfo(serviceId) {
  const modelInfo = getServiceModelInfoFromCache(serviceId) || getServiceModelInfoFromDb(serviceId) || null;
  if (modelInfo) {
    return modelInfo;
  } else {
    trace.info(`Unable to find service descriptor document for service ${serviceId}.`, "getServiceModel");
    throw err.newNotFoundError(`Service descriptor ${serviceId} not found.`);
  }
}

function getServiceModel(serviceId) {
  return getServiceModelInfo(serviceId).model;
}

function saveServiceModel(serviceId, model, uri) {
  declareUpdate();
  // TODO: add validate model before saving
  const _uri = uri || getServiceModelInfo(serviceId).uri;
  trace.info(`Updating service descriptor document \"${serviceId}\" at \"${_uri}\".`, "saveServiceModel");

  const options = {
    collections: xdmp.documentGetCollections(_uri),
    permissions: xdmp.documentGetPermissions(_uri),
    metadata: xdmp.documentGetMetadata(_uri),
    quality: xdmp.documentGetQuality(_uri)
  };
  xdmp.documentInsert(_uri, model, options);

  setModelCache([{
    serviceId: serviceId,
    model: model,
    uri: uri
  }]);
}

exports.getServiceModels = module.amp(getServiceModels);
exports.getServiceModel = module.amp(getServiceModel);
exports.saveServiceModel = module.amp(saveServiceModel);
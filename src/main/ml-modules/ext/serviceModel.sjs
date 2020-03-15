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

function getServiceModelFromCache(serviceId) {
  const modelCache = getModelCache();
  return modelCache[serviceId];
}

function cacheServiceModel(serviceId, model) {
  const modelCache = getModelCache();
  modelCache[serviceId] = model;
  xdmp.setServerField(MODEL_CACHE_NAME, Sequence.from(modelCache));
  return model;
}

function getServiceModelFromDb(serviceId, cacheAfterLoad = true) {
  const model = fn.head(
    cts.search(cts.andQuery([
      cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION),
      cts.jsonPropertyValueQuery("name", serviceId, ["exact"])
    ]))
  );
  if (cacheAfterLoad) {
    cacheServiceModel(serviceId, model);
  }
  return model ? model.toObject() : null;
}

function getServiceModels(filter) {
  const validFilters = new Set(['all', 'search']);
  const _filter = filter || 'all';
  if (!validFilters.has(_filter)) { throw err.newInputError(`Invalid filter '${_filter}'.`)}

  let modelCache = {};
  let models = [];
  const docs = cts.search(cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION)).toArray();
  docs.forEach((doc) => {
    const model = doc.toObject();
    modelCache[model.info.name] = model;
    if (_filter === 'search' && !model.search) { return; }
    models.push(model);
  });
  xdmp.setServerField(MODEL_CACHE_NAME, Sequence.from(modelCache));

  trace.info(`Found ${models.length} service descriptor documents.` + 
    (docs.length != models.length ? `${docs.length - models.length} documents ignored from collection.` : ''), 
    "getServiceModels");
  return models;
}

function getServiceModel(serviceId) {
  const model = getServiceModelFromCache(serviceId) || getServiceModelFromDb(serviceId) || null;
  if (model) {
    return model;
  } else {
    trace.info(`Unable to find service descriptor document for service ${serviceId}.`, "getServiceModel");
    throw err.newNotFoundError(`Service descriptor ${serviceId} not found.`);
  }
}

exports.getServiceModels = getServiceModels;
exports.getServiceModel = getServiceModel;
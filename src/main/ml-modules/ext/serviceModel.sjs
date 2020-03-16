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

function setModelCache(models, replace = false) {
  const modelCache = replace ? {} : xdmp.getServerField(MODEL_CACHE_NAME);
  models.forEach(m => modelCache[m.info.name] = m);
  xdmp.setServerField(MODEL_CACHE_NAME, Sequence.from(modelCache));
}

function getServiceModelFromCache(serviceId) {
  const modelCache = getModelCache();
  return modelCache[serviceId];
}

function getServiceModelFromDb(serviceId, cacheAfterLoad = true) {
  const modelDoc = fn.head(
    cts.search(cts.andQuery([
      cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION),
      cts.jsonPropertyValueQuery("name", serviceId, ["exact"])
    ]))
  );
  const model = modelDoc ? modelDoc.toObject() : null;
  if (cacheAfterLoad && model) {
    setModelCache([ model ]);
  }
  return model;
}

function getServiceModels(filter) {
  const validFilters = new Set(['all', 'search']);
  const _filter = filter || 'all';
  if (!validFilters.has(_filter)) { throw err.newInputError(`Invalid filter '${_filter}'.`)}

  // (re)select all descriptors and cache them
  const allModels = cts.search(cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION))
    .toArray()
    .map(doc => doc.toObject());
  setModelCache(allModels, true);

  trace.info(`Found a total of ${allModels.length} service descriptor documents.`, "getServiceModels");

  return _filter === 'search' ? allModels.filter(m => m.search) : allModels;
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

exports.getServiceModels = module.amp(getServiceModels);
exports.getServiceModel = module.amp(getServiceModel);
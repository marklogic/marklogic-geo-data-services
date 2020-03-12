'use strict';
const err = require('/ext/error.sjs');
const trace = require('/ext/trace.sjs');

const SERVICE_DESCRIPTOR_COLLECTION = 'http://marklogic.com/feature-services';

function getServiceModels(filter) {
  const validFilters = new Set(['all', 'search']);
  const _filter = filter || 'all';
  if (!validFilters.has(_filter)) { throw err.newInputError(`Invalid filter '${_filter}'.`)}

  let models = [];
  const docs = cts.search(cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION)).toArray();
  docs.forEach((doc) => {
    const sd = doc.toObject();
    if (_filter === 'search' && !sd.search) { return; }
    if (sd.info) {
      let model = {
        id: sd.info.name,
        name: sd.info.name,
        description: sd.info.description
      };
      if (sd.search) {
        model.search = Object.keys(sd.search);
      }
      models.push(model);
    }
  });
  trace.info(`Found ${models.length} service descriptor documents.` + 
    (docs.length != models.length ? `${docs.length - models.length} documents ignored from collection.` : ''), 
    "getServiceModels");
  return models;
}

function getServiceModel(serviceName) {
  // TODO: These should be cached
  const model = fn.head(
    cts.search(cts.andQuery([
      cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION),
      cts.jsonPropertyValueQuery("name", serviceName, ["exact"])
    ]))
  );

  if (model) {
    return model.toObject();
  } else {
    trace.info(`Unable to find service descriptor document for service ${serviceName}.`, "getServiceModel");
    throw err.newNotFoundError(`Service descriptor ${serviceName} not found.`);
  }
}

exports.getServiceModels = getServiceModels;
exports.getServiceModel = getServiceModel;
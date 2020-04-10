'use strict';
const sm = require('/ext/serviceModel.sjs');
const err = require('/ext/error.sjs');
const trace = require('/ext/trace.sjs');
const gs = require('/ext/search/geoSearch.sjs');
const searchLib = require('/MarkLogic/appservices/search/search.xqy');

/*
  PUT payload: saving search into a layer
  {
    "params": {
      "id": "service descriptor name", // required; source descriptor model
      "layers": { 
        "source layer ID": {
          "layerId": 5, // required; layer ID to replace or "new" to append, can be the same as source layer ID
          "name": "layer name", // optional; defaults to name of source layer
          "description": "layer description" // optional; defaults to description of source layer
      },
      "debug": false // optional
    },
    "search": { ...search parameters to be saved (same as that of POST -- see geoSearch.sjs)... }
  }
*/

function geoSearchSave(input) {
  if (!input.params) { throw err.newInputError("Missing section 'params'."); }
  if (!input.params.id) { throw err.newInputError("No service descriptor ID provided in the property params.id"); }
  if (!input.search) { throw err.newInputError("missing section 'search'."); }
  if (!input.params.layers || (input.params.layers && Object.keys(input.params.layers).length === 0)) { throw err.newInputError("Missing section 'layers'."); }

  let model = sm.getServiceModel(input.params.id);
  if (!model.search || !model.search.options) { throw err.newInputError(`The service descriptor "${model.info.name}" is not configured for use with geoSearchService: missing search options.`); }

  const debugMode = input.params.debug === true;
  let debug = debugMode ? {} : null;

  const _input = gs.resolveInput({
    params: { id: input.params.id },
    search: input.search
  });
  const criteria = gs.createSearchCriteria(model, _input, false, false, false, debug); // get search:search
  const ctsQuery = searchLib.parse(_input.search.qtext, criteria.xpath('./search:options'), 'cts:query'); // get cts:query
  
  const targets = [];
  if (input.params.layers) {
    Object.keys(input.params.layers).forEach(lid => {
      const sourceLayerId = parseInt(lid);
      if (isNaN(sourceLayerId)) { 
        throw err.newInputError(`One or more source layer IDs is invalid.`);
      }
      targets.push({ 
        sourceLayerId: sourceLayerId,
        ...input.params.layers[lid]
      });
    }); 
  }

  const now = fn.currentDateTime();
  const user = xdmp.getCurrentUser();
  const mutatedLayers = [];
  for (var target of targets) {
    const sourceLayer = model.layers.find(l => l.id === target.sourceLayerId);
    if (!sourceLayer) { throw err.newInputError(`Source layer ${target.sourceLayerId} not found.`); }
    if (!sourceLayer.search || (sourceLayer.search && !sourceLayer.search.geoConstraint)) { throw err.newInputError(`Source layer ${target.sourceLayerId} is not configured for use with geoSearchService; missing search section.`); }

    // replace
    if (target.layerId >= 0) {
      const targetLayer = model.layers.find(l => l.id === target.layerId);
      if (!targetLayer) { throw err.newInputError(`Target layer ${target.layerId} not found.`); }
      if (targetLayer.readOnly) { throw err.newInputError(`Target layer ${target.layerId} is read-only and cannot be replaced.`); }

      targetLayer.name = target.name || targetLayer.name;
      targetLayer.description = target.description || targetLayer.description;
      targetLayer.search = targetLayer.search || Object.assign({}, sourceLayer.search);
      targetLayer.search.sourcedFrom = sourceLayer.id;
      targetLayer.search.lastModifiedOn = now;
      targetLayer.search.lastModifiedBy = user;
      targetLayer.boundingQuery = ctsQuery;
      mutatedLayers.push(target);
    }
    // append
    else if (target.layerId === "new") { 
      const nextLayerId = fn.max(Sequence.from(model.layers.map(l => l.id))) + 1;
      let newLayer = Object.assign({}, sourceLayer); // shallow copy should suffice
      newLayer.id = nextLayerId;
      newLayer.name = target.name || newLayer.name;
      newLayer.description = target.description || newLayer.description;
      newLayer.search.sourcedFrom = sourceLayer.id;
      newLayer.search.lastModifiedOn = now;
      newLayer.search.lastModifiedBy = user;
      newLayer.boundingQuery = ctsQuery;
      model.layers.push(newLayer);

      target.layerId = nextLayerId;
      mutatedLayers.push(target);
    }
    else {
      throw err.newInputError(`The source layer ${target.sourceLayerId} has no specified target layer to be added or replaced.`);
    }
  }

  if (mutatedLayers.length <= 0) {
    throw err.newInternalError(`No layers were modified or added for service descriptor "${model.info.name}".`)
  }
  else {
    sm.saveServiceModel(input.params.id, model);
  }

  const response = {
    id: input.params.id,
    layers: mutatedLayers.reduce((result, layer) => { 
      result[layer.sourceLayerId.toString()] = { 
        layerId: layer.layerId,
        name: layer.name
      };
      return result;
    }, {})
  };

  if (debugMode) {
    response.debug = {
      resolvedModel: model,
      resolvedCtsQuery: ctsQuery,
      ...debug
    };
  }

  return response;
}

exports.geoSearchSave = geoSearchSave;
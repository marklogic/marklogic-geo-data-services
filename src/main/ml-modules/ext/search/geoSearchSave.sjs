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
        "Name of geo constraint": layer number or "new" to append
        "Name of geo constraint": layer number or "new" to append
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
  const modelGeoConstraints = gs.getGeoConstraintNames(model);
  if (modelGeoConstraints.length <= 0) { trace.warn(`The service descriptor "${model.info.name}" has no layers with a geoConstraint.`, "geoSearchSave"); }

  const debugMode = input.params.debug === true;
  let debug = debugMode ? {} : null;

  const _input = gs.resolveInput({
    params: { id: input.params.id },
    search: input.search
  });
  const criteria = gs.createSearchCriteria(model, _input, false, false, false, debug); // get search:search
  const ctsQuery = searchLib.parse(_input.search.qtext, criteria.xpath('./search:options'), 'cts:query'); // get cts:query
  
  const targetLayers = [];
  if (input.params.layers) {
    Object.keys(input.params.layers).forEach((propName) => {
      const targetLayerId = input.params.layers[propName];
      targetLayers.push({ geoConstraint: propName, layerId: targetLayerId === "new" ? -1 : targetLayerId });
    }); 
  }

  const now = fn.currentDateTime();
  const user = xdmp.getCurrentUser();
  const mutatedLayerIds = [];
  const responseWarnings = [];
  for (var targetLayer of targetLayers) {
    if (!modelGeoConstraints.includes(targetLayer.geoConstraint)) {
      trace.warn(`The specified geo constraint "${targetLayer.geoConstraint}" doesn't exist in the search options "${model.search.options}".`, "geoSearchSave");
      responseWarnings.push(`The geo constraint "${targetLayer.geoConstraint}" doesn't exist.`);
      continue;
    }

    // replace
    if (targetLayer.layerId >= 0) {
      let layer = model.layers.find(l => l.id === targetLayer.layerId);
      if (layer) {
        layer.search = {
          geoConstraint: targetLayer.geoConstraint,
          lastModifiedOn: now,
          lastModifiedBy: user
        };
        layer.boundingQuery = ctsQuery;
        mutatedLayerIds.push(layer.id);
      }
      else { 
        trace.warn(`Unable to locate layer with ID = ${targetLayer.layerId} in service descriptor "${model.info.name}" for saving.`, "geoSearchSave");
        responseWarnings.push(`Layer with ID of ${targetLayer.layerId} could not be found.`);
      }
    }
    // append
    else { 
      const nextLayerId = fn.max(Sequence.from(model.layers.map(l => l.id))) + 1;
      const sourceLayer = model.layers.find(l => l.search && l.search.geoConstraint === targetLayer.geoConstraint);
      if (sourceLayer) {
        let newLayer = Object.assign({}, sourceLayer); // shallow copy should suffice
        newLayer.id = nextLayerId;
        newLayer.boundingQuery = ctsQuery;
        newLayer.search = {
          geoConstraint: targetLayer.geoConstraint,
          sourceLayerId: sourceLayer.id,
          lastModifiedOn: now,
          lastModifiedBy: user
        };
        model.layers.push(newLayer);
        mutatedLayerIds.push(newLayer.id);
      }
      else { trace.warn(`Unable to find source layer with geoConstraint "${targetLayer.geoConstraint}" in service descriptor "${model.info.name}".`, "geoSearchSave"); }
    }
  }

  if (mutatedLayerIds.length <= 0) {
    trace.warn(`No layers were modified or added for service descriptor "${model.info.name}".`, "geoSearchSave");
    responseWarnings.push("No layers were modified or added.");
  }
  else {
    sm.saveServiceModel(input.params.id, model);
  }

  const response = {
    id: input.params.id,
    feature: input.params.id,
    layerIds: mutatedLayerIds
  };
  if (responseWarnings.length > 0) { response.warnings = responseWarnings; }

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
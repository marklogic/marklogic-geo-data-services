'use strict';
const sm = require('/ext/serviceModel.sjs');
const err = require('/ext/error.sjs');
const trace = require('/ext/trace.sjs');
const gs = require('/ext/search/geoSearch.sjs');
const searchLib = require('/MarkLogic/appservices/search/search.xqy');

declareUpdate();

/*
  PUT payload: saving search into a layer
  {
    "params": {
      "id": "service descriptor name", // required; source descriptor model
      "layers": { // optional; used to replace existing saved searches.  If not present, appends new layers
        "Name of geo constraint": layer number, 
        "Name of geo constraint": layer number,
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

  let model = sm.getServiceModel(input.params.id);
  if (!(model.search && model.search.options)) { throw err.newInputError(`The service descriptor \"${model.info.name}" is not configured for use with geoSearchService: missing search options.`); }
  const modelGeoConstraints = gs.getGeoConstraintNames(model);
  if (modelGeoConstraints.length <= 0) { trace.warn(`The service descriptor \"${model.info.name}\" has no layers with a geoConstraint.`, "geoSearchSave"); }

  const debugMode = input.params.debug === true;
  let debug = debugMode ? {} : null;

  const _input = gs.resolveInput({
    params: { id: input.params.id },
    search: input.search
  });
  const criteria = gs.createSearchCriteria(model, _input, false, false, false, debug); // get search:search
  const ctsQuery = searchLib.parse(input.search.qtext, criteria.xpath('./search:options'), 'cts:query'); // get cts:query
  
  const targetLayers = [];
  if (input.params.layers) { 
    Object.keys(input.params.layers).forEach((key, value) => targetLayers.push({ geoConstraint: key, layerId: parseInt(value) })); 
  }
  else {
    const nextLayerId = fn.max(Sequence.from(model.layers.map(l => l.id))) + 1;
    modelGeoConstraints.forEach((name, idx) => targetLayers.push({ geoConstraint: name, layerId: nextLayerId + idx }));
  }

  const now = fn.currentDateTime();
  const user = xdmp.getCurrentUser();
  for (var targetLayer of targetLayers) {
    let layer = model.layers.find(l => l.id === targetLayer.layerId);
    if (layer) { // replace
      layer.search = {
        geoConstraint: targetLayer.geoConstraint,
        lastModifiedOn: now,
        lastModifiedBy: user
      };
      layer.boundingQuery = ctsQuery;
    } 
    else { // append
      const sourceLayer = model.layers.find(l => l.search && l.search.geoConstraint === targetLayer.geoConstraint);
      if (sourceLayer) {
        let newLayer = Object.assign({}, sourceLayer); // shallow copy should suffice
        newLayer.id = targetLayer.layerId;
        newLayer.boundingQuery = ctsQuery;
        newLayer.search = {
          geoConstraint: targetLayer.geoConstraint,
          sourceLayerId: sourceLayer.id,
          lastModifiedOn: now,
          lastModifiedBy: user
        };
        model.layers.push(newLayer);
      }
      else {
        trace.warn(`Unable to find source layer with geoConstraint \"${targetLayer.geoConstraint}\" in service descriptor \"${model.info.name}\".`, "geoSearchSave");
      }
    }
  }

  // xdmp.documentInsert() here
  // update cache here

  const response = {
    id: input.params.id,
    feature: input.params.id,
    featureLayerIds: targetLayers.map(l => l.layerId)
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
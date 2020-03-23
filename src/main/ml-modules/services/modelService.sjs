'use strict';
const sm = require('/ext/serviceModel.sjs');
const err = require('/ext/error.sjs');

function get(context, params) {
  try {
    if (params.id) {
      return sm.getServiceModel(params.id);
    }
    else {
      const filter = params.filter || "all";
      let models = {};
      sm.getServiceModels(filter).forEach(model => {
        const geoConstraints = model.layers.filter(l => l.search && l.search.geoConstraint).map(l => l.search.geoConstraint);
        let obj = {
          id: model.info.name,
          name: model.info.name,
          description: model.info.description,
          canSearch: model.hasOwnProperty("search") && geoConstraints.length > 0,
          totalLayers: model.layers ? model.layers.length : 0
        };
        if (obj.canSearch) {
          obj.valueNames = geoConstraints;
          obj.docTransform = model.search.docTransform || "default-geo-data-services-transform";
        }
        models[model.info.name] = obj;
      });
      return {
        models: models
      };
    }
  }
  catch (error) {
    err.handleError(error);
  }
}

exports.GET = get;
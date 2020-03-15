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
        models[model.info.name] = {
          id: model.info.name,
          name: model.info.name,
          description: model.info.description,
          canSearch: model.hasOwnProperty("search"),
          totalLayers: model.layers ? model.layers.length : 0
        };
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
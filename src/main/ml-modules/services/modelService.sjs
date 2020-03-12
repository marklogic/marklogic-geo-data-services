'use strict';
const gds = require('/ext/gds.sjs');
const err = require('/ext/error.sjs');

function get(context, params) {
  try {
    if (params.id) {
      return gds.getServiceModel(params.id);
    }
    else {
      const filter = params.filter || "all";
      return {
        models: gds.getServiceModels(filter)
      };
    }
  }
  catch (error) {
    err.handleError(error);
  }
}

exports.GET = get;
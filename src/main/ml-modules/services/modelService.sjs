'use strict';
const gds = require('/ext/gds.sjs');
const err = require('/ext/error.sjs');

function get(context, params) {
  try {
    if (params.id) {
      return gds.getServiceModel(params.id);
    }
    else {
      return {
        models: gds.getServiceModels()
      };
    }
  }
  catch (error) {
    err.handleError(error);
  }
}

exports.GET = get;
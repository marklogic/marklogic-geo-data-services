'use strict';
const err = require('/ext/error.sjs');
const gs = require('/ext/search/geoSearch.sjs');

function post(context, params, input) {
  try {
    const _input = input.toObject();
    return gs.geoSearch(_input);
  }
  catch (error) {
    err.handleError(error);
  }
}

exports.POST = post;
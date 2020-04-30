'use strict';
const err = require('/ext/error.sjs');
const gs = require('/ext/search/geoSearch.sjs');
const gss = require('/ext/search/geoSearchSave.sjs');

function post(context, params, input) {
  try {
    const _input = input.toObject();
    if (!_input) { throw err.newInputError('No JSON input found in POST body.'); }
    return gs.geoSearch(_input);
  }
  catch (error) {
    err.handleError(error);
  }
}

function put(context, params, input) {
  try {
    const _input = input.toObject();
    if (!_input) { throw err.newInputError('No JSON input found in POST body.'); }
    return gss.geoSearchSave(_input);
  }
  catch (error) {
    err.handleError(error);
  }
}

exports.POST = post;
exports.PUT = put;
/**
 * As of the 1.3.4 release, only used by marklogic-arcgis-pro-addin.
 */
'use strict';
const err = require('/ext/error.sjs');
const gs = require('/ext/search/geoSearch.sjs');
const gss = require('/ext/search/geoSearchSave.sjs');
const gdsVersion = require('/ext/version.sjs').version;

function post(context, params, input) {
  try {
    const _input = input.toObject();
    if (!_input) { throw err.newInputError('No JSON input found in POST body.'); }
    return {
      "$version": gdsVersion,
      ...gs.geoSearch(_input)
    };
  }
  catch (error) {
    err.handleError(error);
  }
}

function put(context, params, input) {
  try {
    const _input = input.toObject();
    if (!_input) { throw err.newInputError('No JSON input found in POST body.'); }
    return {
      "$version": gdsVersion,
      ...gss.geoSearchSave(_input)
    };
  }
  catch (error) {
    err.handleError(error);
  }
}

exports.POST = post;
exports.PUT = put;

'use strict';

/**
 * This REST extension is only used by marklogic-arcgis-pro-addin.
 */

const err = require('/marklogic-geo-data-services/error.sjs');
const geoSearch = require('/marklogic-geo-data-services/geoSearch/geoSearch.sjs');
const geoSearchSave = require('/marklogic-geo-data-services/geoSearch/geoSearchSave.sjs');
const gdsVersion = require('/marklogic-geo-data-services/version.sjs').version;

function post(context, params, input) {
  try {
    const _input = input.toObject();
    if (!_input) { throw err.newInputError('No JSON input found in POST body.'); }
    return {
      "$version": gdsVersion,
      ...geoSearch.geoSearch(_input)
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
      ...geoSearchSave.saveGeoSearch(_input)
    };
  }
  catch (error) {
    err.handleError(error);
  }
}

exports.POST = post;
exports.PUT = put;

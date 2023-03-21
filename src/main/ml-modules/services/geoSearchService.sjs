/*
 * Copyright (c) 2023 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

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
const gdsVersion = require('/marklogic-geo-data-services/version.sjs').version;
const searchUtil = require('/marklogic-geo-data-services/search-util.xqy');
const serviceLib = require('/marklogic-geo-data-services/serviceLib.sjs');

function get(context, params) {
  try {
    if (params.id) {
      return {
        "$version": gdsVersion,
        ...serviceLib.getServiceModel(params.id)
      };
    }
    else {
      const filter = params.filter || "all";
      let models = {};
      serviceLib.getServiceModels(filter).forEach(model => {
        let geoConstraints = [];
        let obj = {
          id: model.info.name,
          name: model.info.name,
          description: model.info.description,
          layers: model.layers.map(layer => {
            let lobj = {
              id: layer.id,
              name: layer.name,
              description: layer.description,
              geometryType: layer.geometryType,
            };
            if (layer.search && layer.search.geoConstraint) {
              lobj.geoConstraint = layer.search.geoConstraint;
              geoConstraints.push(layer.search.geoConstraint);
            }
            return lobj;
          })
        };
        if (geoConstraints.length > 0) {
          const allConstraints = searchUtil.getSearchOptionsConstraints(model.search.options).toArray().map(o => o.toObject());
          obj.search = {
            docTransform: model.search.docTransform || "default-geo-data-services-transform",
            constraints: allConstraints.filter(o => !geoConstraints.includes(o.name)) // don't include geo constraints
          }
        }
        models[model.info.name] = obj;
      });
      return {
        "$version": gdsVersion,
        models: models
      };
    }
  }
  catch (error) {
    err.handleError(error);
  }
}

exports.GET = get;

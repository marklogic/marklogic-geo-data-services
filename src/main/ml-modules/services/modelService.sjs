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

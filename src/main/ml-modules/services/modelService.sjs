'use strict';
const gsu = require('/ext/search/geo-search-util.xqy');
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
          const allConstraints = gsu.getSearchOptionsConstraints(model.search.options).toArray().map(o => o.toObject());
          obj.search = {
            docTransform: model.search.docTransform || "default-geo-data-services-transform",
            constraints: allConstraints.filter(o => !geoConstraints.includes(o.name)) // don't include geo constraints
          }
        }
        models[model.info.name] = obj;
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
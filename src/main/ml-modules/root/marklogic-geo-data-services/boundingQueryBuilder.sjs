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
 * Module for building a bounding query based on a user's query and a layer. All of this was originally in
 * geoQueryService.sjs and was extracted to reduce the size of that module.
 */

const geoExtractor = require('/marklogic-geo-data-services/extractor.sjs');
const geojson = require('/MarkLogic/geospatial/geojson.xqy');
const queryDeserializer = require('/marklogic-geo-data-services/ctsQueryDeserialize.sjs').qd;

/**
 *
 * @param requestQuery the query from the caller's request
 * @param layerModel
 * @returns a CTS query
 */
function buildBoundingQuery(requestQuery, layerModel) {
  const boundingQueries = [ buildQueryFromLayerGeometry(requestQuery, layerModel) ];

  if (layerModel.boundingQuery) {
    boundingQueries.push(queryDeserializer.query(layerModel.boundingQuery));
  }
  if (layerModel.temporalBounds) {
    boundingQueries.push(getTemporalQuery(layerModel.temporalBounds));
  }

  // alwaysIncludeQuery is an optional feature for a layer definition to ensure that certain data is always included,
  // which implies usage of an "or" query. See https://github.com/marklogic/marklogic-geo-data-services/issues/76
  // for the original requirements.
  return layerModel.alwaysIncludeQuery ?
    cts.orQuery([queryDeserializer.query(layerModel.alwaysIncludeQuery), cts.andQuery(boundingQueries)]) :
    cts.andQuery(boundingQueries);
}

/**
 * Builds a query based on the extension/geometry in the given request query and the configuration in the layer model.
 * The marklogic-koop-provider is expected to convert Esri geometry objects into GeoJSON in WGS84 and place it in the
 * query.extension.geometry property.
 *
 * The extractor module is responsible for producing a point query and/or a region query based on the "geometry" object
 * in the layer model.
 *
 * @param requestQuery
 * @param layerModel
 * @returns {*}
 */
function buildQueryFromLayerGeometry(requestQuery, layerModel) {
  let query = cts.trueQuery();
  if (requestQuery.extension && requestQuery.extension.geometry) {
    const mustConvertEnvelope = !requestQuery.geometryType || requestQuery.geometryType.toLowerCase() === "esrigeometryenvelope";
    const regions = mustConvertEnvelope ?
      // handle this because the koop server changes boxes to GeoJSON polygons but a box is better for this
      convertEnvelopePolygon(requestQuery) :
      geojson.parseGeojson(adjustEsriPolygon(requestQuery.extension.geometry));

    const pointQuery = geoExtractor.getPointQuery(regions, layerModel);
    const operation = parseRegionOperation(requestQuery);
    const regionQuery = geoExtractor.getRegionQuery(regions, operation, layerModel);

    const queries = [];
    if (pointQuery) { queries.push(pointQuery); }
    if (regionQuery) { queries.push(regionQuery); }
    if (queries.length > 0) {
      query = cts.orQuery(queries);
    }
  }

  xdmp.trace("GDS-DEBUG", "Initial layer query: " + query);
  return query;
}

function getTemporalQuery(temporalReference) {
  if (temporalReference == null)
    return cts.trueQuery();

  let geQuery = null;
  let leQuery = null;
  if (temporalReference.referenceType == "field") {
    if (temporalReference.ge) {
      geQuery = cts.fieldRangeQuery(
        temporalReference.fieldName,
        ">=",
        getDateTime(temporalReference.ge));

    }
    if (temporalReference.le) {
      leQuery = cts.fieldRangeQuery(
        temporalReference.fieldName,
        "<=",
        getDateTime(temporalReference.le));
    }
  }

  if (geQuery && leQuery)
    return cts.andQuery([geQuery, leQuery]);
  else if (geQuery)
    return geQuery;
  else if (leQuery)
    return leQuery;
  else
    return cts.trueQuery();
};

function getDateTime(durationOrTimestamp) {
  return fn.head(
    xdmp.xqueryEval(`
      declare variable $d external;
      if (xdmp:castable-as("http://www.w3.org/2001/XMLSchema", "dayTimeDuration", $d)) then
        fn:current-dateTime() + xs:dayTimeDuration($d)
      else if (xdmp:castable-as("http://www.w3.org/2001/XMLSchema", "yearMonthDuration", $d)) then
        fn:current-dateTime() + xs:yearMonthDuration($d)
      else if (xdmp:castable-as("http://www.w3.org/2001/XMLSchema", "dateTime", $d)) then
        xs:dateTime($d)
      else
        ()
    `,
      {"d": durationOrTimestamp})
  );
}

function convertEnvelopePolygon(query) {
  // the koop server converts Esri envelopes to GeoJSON polygons

  // convert them to boxes for more efficient seach
  // TODO: file an issue about the winding order (they do not follow the right hand rule)
  //{
  //  type: 'Polygon',
  //  coordinates: [[
  //    [geom.xmin, geom.ymin],
  //    [geom.xmin, geom.ymax],
  //    [geom.xmax, geom.ymax],
  //    [geom.xmax, geom.ymin],
  //    [geom.xmin, geom.ymin]
  //  ]]
  //}

  const geometry = adjustEsriBox(query.extension.geometry);
  const coords = adjustEsriPolygon(geometry).coordinates[0];
  const south = coords[0][1];
  const west = coords[0][0];
  const north = coords[2][1];
  const east = coords[2][0];
  const box = { south : south, west : west, north : north, east : east }

  return splitBox(box).toArray();
}

function adjustEsriBox(esriBox) {
  if (esriBox.hasOwnProperty("spatialReference") && esriBox.spatialReference.wkid == 4326) {
    if (esriBox.ymin <= -90)  esriBox.ymin =  -89.999;
    if (esriBox.xmin <= -180) esriBox.xmin = -179.999;
    if (esriBox.ymax >= 90)   esriBox.ymax =   89.999;
    if (esriBox.xmax >= 180)  esriBox.xmax =  179.999;
  }
  return esriBox;
}

function adjustEsriPolygon(esriPolygon) {
  if (esriPolygon.hasOwnProperty("spatialReference") && esriPolygon.spatialReference.wkid == 4326) {
    adjustCoordinatesArrayR(esriPolygon.coordinates);
  }
  return esriPolygon;
}

function adjustCoordinatesArrayR(coords) {
  if (coords.length === 2 && coords[0].constructor == Number && coords[1].constructor == Number) {
    clampPointArray(coords);
  } else if (Array.isArray(coords[0])) { //assume it's an array of arrays, we won't get a mix
    coords.forEach(arr => {
      adjustCoordinatesArrayR(arr);
    });
  }
}

function clampPointArray(coord) {
  // clamps [lon, lat] array to maximum values
  if (coord[1] >= 90.0)       coord[1] = 90;
  else if (coord[1] <= -90.0) coord[1] = -90;

  if (coord[0] >= 180.0)       coord[0] = 180;
  else if (coord[0] <= -180.0) coord[0] = -180;
}

function splitBox(box) {
  if (Math.round(Math.abs(box.west - box.east)) >= 179) {
    // check east/west
    const middle = (box.west + box.east) / 2.0;
    return Sequence.from([
      { south : box.south, west : box.west, north : box.north, east : middle },
      { south : box.south, west : middle, north : box.north, east : box.east }
    ].map(splitBox))
  } else if (Math.round(Math.abs(box.south - box.north)) >= 179) {
    // check north/south
    const middle = (box.south + box.north) / 2.0;
    return Sequence.from([
      { south : box.south, west : box.west, north : middle, east : box.east },
      { south : middle, west : box.west, north : box.north, east : box.east }
    ].map(splitBox))
  }
  return Sequence.from([ cts.box(box.south, box.west, box.north, box.east) ]);
}

function parseRegionOperation(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseRegionOperation");
  // cts region operators: contains, covered-by, covers, disjoint, intersects, overlaps, within
  // default to intersects

  // TODO: verify mapping of Esri spatial relations to MarkLogic operations
  // can we implement the other Esri relations with combinations of the MarkLogic
  // operations?

  if (query.spatialRel) {
    switch(query.spatialRel.toLowerCase()) {
      case "esrispatialrelintersects":
        return "intersects";
      case "esrispatialrelcontains":
        return "contains";
      case "esrispatialrelcrosses":
        return "crosses";
      case "esrispatialrelwithin":
        return "within";
      case "esrispatialreloverlaps":
        return "overlaps";
      case "esrispatialreltouches":
        return "touches";
      case "esrispatialrelenvelopeintersects":
      case "esrispatialrelindexintersects":
      case "esrispatialrelrelation":
      default:
        throw "Unsupported geospatial operation: " + query.spatialRel;
    }
  } else {
    return "intersects";
  }
}

module.exports = {
  buildBoundingQuery
}
